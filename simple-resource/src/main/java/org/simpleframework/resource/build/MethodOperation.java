package org.simpleframework.resource.build;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.simpleframework.module.build.Argument;
import org.simpleframework.module.build.Function;
import org.simpleframework.module.build.Parameter;
import org.simpleframework.resource.action.Operation;

public class MethodOperation implements Operation {
   
   private final MethodPattern pattern;
   private final MethodMatcher matcher;
   private final MethodHeader header;
   private final Function function;
   
   public MethodOperation(MethodMatcher matcher, MethodHeader header, Function function) {
      this.pattern = matcher.pattern();
      this.function = function;
      this.header = header;
      this.matcher = matcher;
   }
   
   @Override
   public List<Argument> getArguments() {
      Parameter[] parameters = function.getParameters();
      
      if(parameters.length > 0) {
         return Arrays.asList(parameters);
      }
      return Collections.emptyList();
   }
   
   @Override
   public Map<String, String> getHeaders() {
      return header.headers();
   }
   
   @Override
   public String getDescription() {
      return function.getName();
   }

   @Override
   public String getMethod() {
      return matcher.verb();
   }

   @Override
   public String getPath() {
      return pattern.path();
   }
}
