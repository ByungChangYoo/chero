package org.simpleframework.module.resource.action.extract;

import java.util.Arrays;
import java.util.List;

import org.simpleframework.http.Request;
import org.simpleframework.module.build.extract.Parameter;
import org.simpleframework.module.build.extract.StringConverterExtractor;
import org.simpleframework.module.context.Context;
import org.simpleframework.module.context.Model;
import org.simpleframework.module.resource.annotation.HeaderParam;

public class HeaderExtractor extends StringConverterExtractor {

   public HeaderExtractor() {
      super(HeaderParam.class);
   }
   
   @Override
   public List<String> resolve(Parameter parameter, Context context) {
      HeaderParam annotation = parameter.getAnnotation(HeaderParam.class);
      
      if(annotation != null) {
         Model model = context.getModel();
         Request request = model.get(Request.class);
         
         if(request == null) {
            throw new IllegalStateException("Could not get request from model");
         }
         String name = annotation.value();
         String substitute = parameter.getDefault();
         String value = request.getValue(name);
         
         if(value != null) {
            return Arrays.asList(value);
         }
         if(substitute != null) {
            return Arrays.asList(substitute);
         }
      }
      return null;
   }
}