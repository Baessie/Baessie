package org.baessie.simulator.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

public class MapNamspaceContext implements NamespaceContext {

   private final Map<String, String> map;

   public MapNamspaceContext(final Map<String, String> map) {
      this.map = map;
   }

   @Override
   public String getPrefix(final String namespaceURI) {
      String prefix = null;
      if (namespaceURI != null) {
         loop: for (final Entry<String, String> entry : map.entrySet()) {
            if (namespaceURI.equals(entry.getValue())) {
               prefix = entry.getKey();
               break loop;
            }
         }
      }
      return prefix;
   }

   @Override
   public String getNamespaceURI(final String prefix) {
      final String namespaceURI = map.get(prefix);
      return namespaceURI;
   }

   @Override
   public Iterator getPrefixes(final String namespaceURI) {
      return null;
   }

}
