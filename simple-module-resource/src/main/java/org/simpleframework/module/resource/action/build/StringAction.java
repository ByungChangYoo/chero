package org.simpleframework.module.resource.action.build;

import org.simpleframework.module.context.Context;
import org.simpleframework.module.resource.action.Action;

public class StringAction implements Action {
   
   private final String name;
   
   public StringAction(String name) {
      this.name = name;
   }

   @Override
   public Object execute(Context context) {
      return name;
   }

}