package zenv.slot.test.file;

import zenv.slot.utils.FileUtils;
import zenv.slot.conf.Constant;

import java.io.File;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/9 9:08
 */
public class FileRenameTest {
    public static void main(String[] args) {
        FileUtils.renameSlotTempCSV(new File("f:/slot/data"), Constant.SLOT_CSV_TEMP_SUFFIX);
    }
}
