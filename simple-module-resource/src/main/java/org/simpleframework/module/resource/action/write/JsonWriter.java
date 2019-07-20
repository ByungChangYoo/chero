package org.simpleframework.module.resource.action.write;

import static org.simpleframework.module.resource.MediaType.APPLICATION_JSON;
import static org.simpleframework.module.resource.MediaType.TEXT_JSON;

import java.io.OutputStream;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Response;
import org.simpleframework.module.extract.StringConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonWriter implements BodyWriter<Object> {

   private final StringConverter converter;
   private final ObjectMapper mapper;
   
   public JsonWriter(ObjectMapper mapper) {
      this.converter = new StringConverter();
      this.mapper = mapper;
   }
   
   @Override
   public boolean accept(Response response, Object result) throws Exception {
      ContentType type = response.getContentType();
      
      if(type != null && result != null) {
         Class real = result.getClass();
         String value = type.getType();
         String primary = type.getPrimary();
         String secondary = type.getSecondary();
         
         if(value.equalsIgnoreCase(APPLICATION_JSON.value)) {
            return !converter.accept(real);
         }
         if(value.equalsIgnoreCase(TEXT_JSON.value)) {
            return !converter.accept(real);
         }
         if(primary.equalsIgnoreCase(APPLICATION_JSON.primary)) {
            int index = secondary.indexOf("+");
            
            if(index > 0) {
               int length = secondary.length();
               String token = secondary.substring(index + 1, length);
               
               if(token.equalsIgnoreCase(APPLICATION_JSON.secondary)) {
                  return !converter.accept(real);
               }
            }
         }
      }
      return false;
   }

   @Override
   public boolean write(Response response, Object result) throws Exception {
      OutputStream output = response.getOutputStream();
      
      if(result != null) {
         mapper.writeValue(output, result);     
      }      
      return true;
   }

}
