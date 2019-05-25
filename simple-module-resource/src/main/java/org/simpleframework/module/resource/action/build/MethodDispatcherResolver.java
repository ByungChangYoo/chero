package org.simpleframework.module.resource.action.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.simpleframework.module.common.Cache;
import org.simpleframework.module.common.LeastRecentlyUsedCache;
import org.simpleframework.module.context.Context;

import com.google.common.collect.Multimap;

public class MethodDispatcherResolver implements MethodResolver {

   private final Cache<String, MatchGroup> cache;
   private final List<Match> matches;
   private final ComponentFinder finder;
   private final ActionScanner scanner;
   private final PathResolver resolver;
   
   public MethodDispatcherResolver(ActionScanner scanner, ComponentFinder finder) {
      this.cache = new LeastRecentlyUsedCache<String, MatchGroup>(1000);
      this.matches = new LinkedList<Match>();
      this.resolver = new PathResolver();
      this.scanner = scanner;
      this.finder = finder;
   }

   @Override
   public synchronized MethodDispatcher resolveBest(Context context) throws Exception {
      MatchGroup group = match(context);

      if (group != null) {
         MethodDispatcher result = null;
         float best = 0f;

         for (Match match : group.matches) {
            String pattern = match.expression();
            int length = pattern.length();
            
            for (MethodDispatcher dispatcher : match.dispatchers) {
               float score = dispatcher.score(context);
               float value = score + length;
               
               if (value > best) {
                  result = dispatcher;
                  best = value;
               }
            }
         }
         if (result == null) {
            for (Match match : group.matches) {
               for (MethodDispatcher dispatcher : match.dispatchers) {
                  float score = dispatcher.score(context);
                  
                  if(score >= 0) {
                     return dispatcher;
                  }
               }
            }
         }
         return result;
      }
      return null;
   }

   @Override
   public synchronized List<MethodDispatcher> resolveBestFirst(Context context) throws Exception {
      MatchGroup group = match(context);

      if (group != null) {
         List<MethodDispatcher> list = new ArrayList<MethodDispatcher>();

         for (Match match : group.matches) {
            for (MethodDispatcher dispatcher : match.dispatchers) {
               float score = dispatcher.score(context);

               if (score != -1) {
                  list.add(dispatcher);
               }
            }
         }
         return list;
      }
      return Collections.emptyList();
   }

   @Override
   public synchronized List<MethodDispatcher> resolveBestLast(Context context) throws Exception {
      MatchGroup group = match(context);

      if (group != null) {
         LinkedList<MethodDispatcher> list = new LinkedList<MethodDispatcher>();

         for (Match match : group.matches) {
            for (MethodDispatcher dispatcher : match.dispatchers) {
               float score = dispatcher.score(context);

               if (score != -1) {
                  list.addFirst(dispatcher);
               }
            }
         }
         return list;
      }
      return Collections.emptyList();
   }

   private synchronized MatchGroup match(Context context) throws Exception {
      String normalized = resolver.resolve(context);

      if (!cache.contains(normalized)) {
         List<Match> matches = matches();

         if (!matches.isEmpty()) {
            MatchGroup group = new MatchGroup(normalized);

            for (Match match : matches) {
               if (match.matches(normalized)) {
                  group.add(match);
               }
            }
            cache.cache(normalized, group);
         }
      }
      return cache.fetch(normalized);
   }

   private synchronized List<Match> matches() throws Exception {
      if (matches.isEmpty()) {
         Set<Class> components = finder.getComponents();

         for (Class component : components) {
            Multimap<String, MethodDispatcher> extracted = scanner.createDispatchers(component);
            Set<String> patterns = extracted.keySet();

            for (String pattern : patterns) {
               Collection<MethodDispatcher> dispatchers = extracted.get(pattern);

               if (!dispatchers.isEmpty()) {
                  Match match = new Match(dispatchers, pattern);
                  matches.add(match);
               }
            }
         }
         order(matches);
      }
      return matches;
   }

   private synchronized void order(List<Match> matches) throws Exception {
      Collections.sort(matches);

      for (Match match : matches) {
         String text = match.toString();
         int length = text.length();
         
         if(length > 0) {
            System.out.println(text);
         }
      }
   }

   private static class MatchGroup implements Iterable<Match> {

      private final List<Match> matches;
      private final String path;

      public MatchGroup(String path) {
         this.matches = new ArrayList<Match>();
         this.path = path;
      }

      public Iterator<Match> iterator() {
         return matches.iterator();
      }

      public void add(Match match) {
         matches.add(match);
         Collections.sort(matches); // slow ?
      }
   }

   private static class Match implements Comparable<Match> {

      private final Collection<MethodDispatcher> dispatchers;
      private final String expression;
      private final Pattern pattern;
      private final int length;

      public Match(Collection<MethodDispatcher> dispatchers, String pattern) {
         this.pattern = Pattern.compile(pattern);
         this.length = pattern.length();
         this.dispatchers = dispatchers;
         this.expression = pattern;
      }
      
      public String expression() {
         return pattern.pattern();
      }

      public Collection<MethodDispatcher> actions() {
         return dispatchers;
      }

      public void insert(MethodDispatcher action) {
         dispatchers.add(action);
      }

      public boolean matches(String path) {
         Matcher matcher = pattern.matcher(path);

         if (matcher.matches()) {
            return true;
         }
         return false;
      }

      @Override
      public int compareTo(Match match) {
         if (length < match.length) {
            return 1;
         }
         if (length == match.length) {
            return 0;
         }
         return -1;
      }

      @Override
      public String toString() {
         return String.format("'%s' -> %s", expression, dispatchers);
      }
   }
}
