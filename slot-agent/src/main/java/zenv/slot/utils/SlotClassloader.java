package zenv.slot.utils;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/22 18:44
 */
public class SlotClassloader extends URLClassLoader {
    public SlotClassloader(URL[] urls) {
        super(urls);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            return super.loadClass(name, resolve);
        }
        try {
            final Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            // do noting
        }
        return super.loadClass(name, resolve);
    }
}
