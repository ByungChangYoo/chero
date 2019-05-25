package org.simpleframework.module.resource.action.build;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.module.annotation.Component;
import org.simpleframework.module.build.ConstructorScanner;
import org.simpleframework.module.build.Function;
import org.simpleframework.module.core.ComponentManager;
import org.simpleframework.module.core.ComponentStore;
import org.simpleframework.module.core.Context;
import org.simpleframework.module.extract.Extractor;
import org.simpleframework.module.resource.action.ActionContextBuilder;

import junit.framework.TestCase;

public class ListOfComponentsTest extends TestCase {
   
   @Component
   public static class SomeComponent implements Serializable {
      
   }
   
   @Component
   public static class OtherComponent implements Serializable {
      
      private final SomeComponent a;
      private final YetAnotherComponent b;
      
      public OtherComponent(SomeComponent a, YetAnotherComponent b) {
         this.a = a;
         this.b = b;
      }
   }
   
   @Component
   public static class YetAnotherComponent implements Serializable {
      
      private SomeComponent a;
   
      public YetAnotherComponent(SomeComponent a) {
         this.a = a;
      }
   }
   
   @Component
   public static class SomeOtherComponent {
      
      private final SomeComponent a;
      private final OtherComponent b;
      
      public SomeOtherComponent(SomeComponent a, OtherComponent b) {
         this.a = a;
         this.b = b;
      }
   }
   
   @Component
   public static class ListOfSerializable {
      
      private final List<Serializable> list;
      
      public ListOfSerializable(List<Serializable> list) {
         this.list = list;
      }
   }

   public void testDepdencyInjection() throws Exception{
      List<Extractor> extractors = new LinkedList<Extractor>();
      ComponentManager dependencySystem = new ComponentStore();
      ComponentFilter filter = new ComponentFilter();
      ConstructorScanner constructorScanner = new ConstructorScanner(dependencySystem, extractors, filter);
      List<Function> builders = constructorScanner.createConstructors(SomeOtherComponent.class);
      MockRequest request = new MockRequest("GET", "/?a=A", "");
      MockResponse response = new MockResponse();
      Context context = new ActionContextBuilder().build(request, response);
      Function builder = builders.iterator().next();
      SomeOtherComponent value = builder.getValue(context);
      
      assertNotNull(value);
      assertNotNull(value.a);
      assertNotNull(value.b);
      assertNotNull(value.b.a);
      assertNotNull(value.b.b);
      assertNotNull(value.b.b.a);
      
      List<Serializable> serializables = dependencySystem.resolveAll(Serializable.class);
      
      assertNotNull(serializables);
      assertFalse(serializables.isEmpty());
   }
   
   public void testDepdencyInjectionListOfComponents() throws Exception{
      List<Extractor> extractors = new LinkedList<Extractor>();
      ComponentManager dependencySystem = new ComponentStore();
      ComponentFilter filter = new ComponentFilter();
      ConstructorScanner constructorScanner = new ConstructorScanner(dependencySystem, extractors, filter);
      List<Function> builders = constructorScanner.createConstructors(SomeOtherComponent.class);
      List<Function> listBuilders = constructorScanner.createConstructors(ListOfSerializable.class);
      MockRequest request = new MockRequest("GET", "/?a=A", "");
      MockResponse response = new MockResponse();
      Context context = new ActionContextBuilder().build(request, response);
      Function builder = builders.iterator().next();
      SomeOtherComponent value = builder.getValue(context);
      
      assertNotNull(value);
      assertNotNull(value.a);
      assertNotNull(value.b);
      assertNotNull(value.b.a);
      assertNotNull(value.b.b);
      assertNotNull(value.b.b.a);
      
      Function listBuilder = listBuilders.iterator().next();
      ListOfSerializable listOfSerializable = listBuilder.getValue(context);
      
      assertNotNull(listOfSerializable);
   }
}