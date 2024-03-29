package zenv.slot.utils;

/**
 * 1||0---0000000000 0000000000 0000000000 0000000000 0 --- 00000 ---00000 ---000000000000
 * <p>
 * 0x1位(占位) 0x41位(时间戳) 0x5位(机房标识) 0x5位(机器标识) 0x12位(序列号)
 * <p>
 * 分布式按时间递增的ID生成器
 * <p>
 * A = 0011 1100
 * <p>
 * B = 0000 1101
 * <p>
 * -----------------
 * <p>
 * A&b = 0000 1100
 * <p>
 * A | B = 0011 1101
 * <p>
 * A ^ B = 0011 0001
 * <p>
 * ~A= 1100 0011
 * <p>
 * ^  如果相对应位值相同，则结果为0，否则为1  (A ^ B)得到49，即 0011 0001
 * <p>
 * << 按位左移运算符。左操作数按位左移右操作数指定的位数。    A << 2得到240，即 1111 0000
 * <p>
 * |  如果相对应位都是0，则结果为0，否则为1  (A | B)得到61，即 0011 1101
 * <p>
 * &  如果相对应位都是1，则结果为1，否则为0  (A & B)，得到12，即0000 1100
 * <p>
 * System.out.println(-1L ^ (-1L << 41)); 最大毫秒数存储位41个1
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/5 17:13
 */
public final class SequenceIdGenerator {
    //开始该类生成ID的时间截，1288834974657 (Thu, 04 Nov 2010 01:42:54 GMT) 这一时刻到当前时间所经过的毫秒数，占 41 位（还有一位是符号位，永远为 0）。
    private final long startTime = System.currentTimeMillis();

    //机器id所占的位数
    private final long workerIdBits = 5L;

    //数据标识id所占的位数
    private final long datacenterIdBits = 5L;

    //机房标识最大31
    private final long datacenterId;

    //机器标识最大31
    private final long workerId;

    //Id生成序列号 从0开始
    private volatile long sequence = 0L;

    //最后一次生成Id的时间戳
    private volatile long lastTimestamp;

    public SequenceIdGenerator(long workerId, long datacenterId) {
        //支持的最大机器id，结果是31,这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数（不信的话可以自己算一下，记住，计算机中存储一个数都是存储的补码，结果是负数要从补码得到原码）
        long maxWorkerId = ~(-1L << workerIdBits);
        if (workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException(
                    String.format("workerId[%d] is less than 0 or greater than maxWorkerId[%d].", workerId, maxWorkerId));
        }
        //支持的最大数据标识id
        long maxDatacenterId = ~(-1L << datacenterIdBits);
        if (datacenterId < 0 || datacenterId > maxDatacenterId) {
            throw new IllegalArgumentException(
                    String.format("datacenterId[%d] is less than 0 or greater than maxDatacenterId[%d].", datacenterId, maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * @return id
     */
    public synchronized long nextId() {
        //当前时间戳
        long timestamp = timeGen();

        //如果当前时间戳小于最后使用时间 则说明系统时钟变慢了
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  " +
                            "Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //(4095+1) & (4095) = 0
        //序列在id中占的位数
        long sequenceBits = 12L;
        //生成序列的最大值(4095)，这里(-1L ^ (-1L << sequenceBits)) = 1111 1111 1111
        long sequenceMask = ~(-1L << sequenceBits);
        //如果序列号超过最大值则获取下一时刻时间戳
        sequence = (sequence + 1) & sequenceMask;
        if (sequence == 0) {
            timestamp = tilNextMillis(lastTimestamp);
        }

        //更新最后访问时间
        lastTimestamp = timestamp;

        //机器id向左移12位
        //数据标识id向左移17位
        long datacenterIdLeftShift = workerIdBits + sequenceBits;
        //时间截向左移5+5+12=22位
        long timestampLeftShift = datacenterIdBits + datacenterIdLeftShift;
        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - startTime) << timestampLeftShift)
                | (datacenterId << datacenterIdLeftShift)
                | (workerId << sequenceBits)
                | sequence;
    }

    /**
     * 获取下一时刻时间戳
     *
     * @param lastTimestamp 最后访问时间戳
     * @return 下一时刻时间戳
     */
    protected long tilNextMillis(final long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取系统时间
     *
     * @return 时间戳毫秒
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /*public static void main(String[] args) {
        SequenceIdGenerator idGenerator = new SequenceIdGenerator(1, 1);
        for (int i = 0; i < 1000000; i++) {
            System.out.println(idGenerator.nextId());
        }
    }*/
}
