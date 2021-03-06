package org.simpleframework.module;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Set;

import org.simpleframework.module.argument.ContextBuilder;
import org.simpleframework.module.core.Context;
import org.simpleframework.module.index.ClassPathBuilder;
import org.simpleframework.module.path.ClassPath;

public class DriverLoader<T> {
   
   private final Class<? extends Driver<T>> type;
   private final ClassPathBuilder builder;
   private final ContextBuilder loader;

   public DriverLoader(Class<? extends Driver<T>> type, Set<Class> modules, Set<String> files) {
      this(type,  modules, files, Collections.EMPTY_SET);
   }
   
   public DriverLoader(Class<? extends Driver<T>> type, Set<Class> modules, Set<String> files, Set<String> paths) {
      this.loader = new ContextBuilder(files, paths);
      this.builder = new ClassPathBuilder(modules);
      this.type = type;
   }
   
   public T create(String... arguments) {
      try {
         Context context = loader.create(arguments);
         ClassPath path = builder.create();
         Constructor<? extends Driver<T>> constructor = type.getDeclaredConstructor();
         
         if(!constructor.isAccessible()) {
            constructor.setAccessible(true);
         }
         return constructor.newInstance().create(path, context);
      } catch(Exception e) {
         throw new IllegalStateException("Could not create application " + type, e);
      }
   }
}
