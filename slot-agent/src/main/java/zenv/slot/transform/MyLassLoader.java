package zenv.slot.transform;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author zhengwei AKA zenv
 * @since 2022/10/17 16:13
 */
public class MyLassLoader  extends URLClassLoader {
    public MyLassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
