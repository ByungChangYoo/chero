package org.simpleframework.module.resource.action.build;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.module.ComponentManager;
import org.simpleframework.module.DependencyManager;
import org.simpleframework.module.annotation.Inject;
import org.simpleframework.module.build.ComponentFinder;
import org.simpleframework.module.build.extract.Extractor;
import org.simpleframework.module.build.extract.ModelExtractor;
import org.simpleframework.module.context.Context;
import org.simpleframework.module.context.Model;
import org.simpleframework.module.resource.action.ActionContextBuilder;
import org.simpleframework.module.resource.action.build.ActionScanner;
import org.simpleframework.module.resource.action.build.MethodDispatcher;
import org.simpleframework.module.resource.action.build.MethodDispatcherResolver;
import org.simpleframework.module.resource.action.extract.CookieExtractor;
import org.simpleframework.module.resource.action.extract.HeaderExtractor;
import org.simpleframework.module.resource.action.extract.PartExtractor;
import org.simpleframework.module.resource.action.extract.QueryExtractor;
import org.simpleframework.module.resource.action.extract.RequestExtractor;
import org.simpleframework.module.resource.action.extract.ResponseExtractor;
import org.simpleframework.module.resource.annotation.Intercept;
import org.simpleframework.module.resource.annotation.QueryParam;

import junit.framework.TestCase;

public class MethodResolverOrderTest extends TestCase {

   @Intercept("/a/b/c")
   public static class Longest {
      @QueryParam("x")
      String x;
      String y;

      public Longest(@QueryParam("y") String y) {
         this.y = y;
      }

      Response response;

      @Intercept("/.*")
      public void fun(Model model) {
         List list = (List) model.get("list");
         list.add(this);
      }
   }

   @Intercept("/a/b")
   public static class Middle {
      @Inject
      Request request;
      @Inject
      Response response;

      @Intercept("/.*")
      public void fun(Model model) {
         List list = (List) model.get("list");
         list.add(this);
      }
   }

   @Intercept("/a")
   public static class Shortest {
      @Inject
      Request request;
      Response response;

      @Intercept("/.*")
      public void fun(Model model) {
         List list = (List) model.get("list");
         list.add(this);
      }
   }

   @Intercept("/")
   public static class AlsoShortest {
      @Inject
      Request request;

      @Intercept("/a")
      public void fun(Model model) {
         List list = (List) model.get("list");
         list.add(this);
      }
   }

   public void testOrder() throws Throwable {
      List<Extractor> extractors = new LinkedList<Extractor>();
      extractors.add(new RequestExtractor());
      extractors.add(new ResponseExtractor());
      extractors.add(new ModelExtractor());
      extractors.add(new QueryExtractor());
      extractors.add(new CookieExtractor());
      extractors.add(new HeaderExtractor());
      extractors.add(new PartExtractor());
      DependencyManager dependencySystem = new ComponentManager();
      ComponentFinder finder = new ComponentFinder(AlsoShortest.class, Longest.class, Middle.class, Shortest.class);
      ActionScanner scanner = new ActionScanner(dependencySystem, extractors);
      MethodDispatcherResolver resolver = new MethodDispatcherResolver(scanner, finder);
      MockRequest request = new MockRequest("GET", "/a/b/c/d/blah?x=X&y=Y", "");
      MockResponse response = new MockResponse(System.out);
      Context context = new ActionContextBuilder().build(request, response);
      List list = new ArrayList();
      context.getModel().set("list", list);

      MethodDispatcher dispatcher = resolver.resolveBest(context);
      dispatcher.execute(context);
      
      assertEquals(((Longest)context.getModel().get(Longest.class)).response, null);
      assertEquals(((Longest)context.getModel().get(Longest.class)).x, "X");
      assertEquals(((Longest)context.getModel().get(Longest.class)).y, "Y");

      assertEquals(list.size(), 1);
      assertEquals(list.get(0).getClass(), Longest.class);

      list.clear();
      assertTrue(list.isEmpty());

      List<MethodDispatcher> bestFirst = resolver.resolveBestFirst(context);

      assertTrue(list.isEmpty());
      assertFalse(bestFirst.isEmpty());
      assertEquals(bestFirst.size(), 3);

      for (MethodDispatcher entry : bestFirst) {
         entry.execute(context);
      }
      assertFalse(list.isEmpty());
      assertEquals(list.size(), 3);
      assertEquals(list.get(0).getClass(), Longest.class);
      assertEquals(list.get(1).getClass(), Middle.class);
      assertEquals(list.get(2).getClass(), Shortest.class);

      list.clear();
      assertTrue(list.isEmpty());

      List<MethodDispatcher> bestLast = resolver.resolveBestLast(context);

      assertTrue(list.isEmpty());
      assertFalse(bestLast.isEmpty());
      assertEquals(bestLast.size(), 3);

      for (MethodDispatcher entry : bestLast) {
         entry.execute(context);
      }
      assertFalse(list.isEmpty());
      assertEquals(list.size(), 3);
      assertEquals(list.get(0).getClass(), Shortest.class);
      assertEquals(list.get(1).getClass(), Middle.class);
      assertEquals(list.get(2).getClass(), Longest.class);

      assertFalse(list.isEmpty());
      assertEquals(list.size(), 3);
      assertEquals(((Shortest) list.get(0)).request, request);
      assertEquals(((Shortest) list.get(0)).response, null);
      assertEquals(((Middle) list.get(1)).request, request);
      assertEquals(((Middle) list.get(1)).response, response);
      assertEquals(((Longest) list.get(2)).response, null);
      assertEquals(((Longest) list.get(2)).x, "X");
      assertEquals(((Longest) list.get(2)).y, "Y");
   }

}