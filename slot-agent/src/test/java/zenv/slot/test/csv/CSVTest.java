package zenv.slot.test.csv;

import zenv.slot.conf.Constant;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/18 10:44
 */
public class CSVTest {
    public static void main(String[] args) {
        /*final SlotOutput slotOutput = new SlotOutput();
        slotOutput.setTraceId(UUID.randomUUID().toString());
        slotOutput.setSpanId(UUID.randomUUID().toString());
        slotOutput.setProduct("kuhn");
        slotOutput.setWhat("method1");
        slotOutput.setSuccess(true);
        slotOutput.setWhere("zenv.slot.utils.CsvUtils@method1");
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            FileUtils.writeStrOut(CsvUtils.getCsvContentStr(slotOutput), "f:/test.csv");
        }
        System.out.println(System.currentTimeMillis() - time);*/

        /*final List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        for (int i = list.size() - 1; i >= 0; i--) {
            final Integer item = list.get(i);
            if (item % 2 == 1) {
                list.remove(item);
            }
        }
        System.out.println(list);*/

        String s = "slot.class.zenv.slot";
        System.out.println(s.substring(Constant.CLASS_PREFIX.length()));
    }
}
