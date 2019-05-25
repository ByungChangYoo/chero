package org.simpleframework.module.resource.action.build;

import org.simpleframework.http.Request;
import org.simpleframework.module.build.Function;
import org.simpleframework.module.context.Context;
import org.simpleframework.module.context.Model;

public class MethodDispatcher {

   private final MethodExecutor executor;

   public MethodDispatcher(MethodMatcher matcher, MethodHeader header, Function function) {
      this.executor = new MethodExecutor(matcher, header, function);
   }

   public Object execute(Context context) throws Exception {
      Model model = context.getModel();
      Request request = model.get(Request.class);
      
      if(request == null) {
         throw new IllegalStateException("Could not get request from model");
      }
      return executor.execute(context);
   }

   public float score(Context context) throws Exception {
      return executor.score(context);
   }

   @Override
   public String toString() {
      return executor.toString();
   }
}
