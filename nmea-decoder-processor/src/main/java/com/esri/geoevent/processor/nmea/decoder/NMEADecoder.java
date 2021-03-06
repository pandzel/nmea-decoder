/*
  Copyright 2019 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.processor.nmea.decoder;

import com.esri.geoevent.processor.nmea.decoder.translator.NMEAMessageTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

public class NMEADecoder extends GeoEventProcessorBase {

  private static final Log LOG = LogFactory.getLog(NMEADecoder.class);

  private final GeoEventCreator geoEventCreator;
  private final GeoEventDefinitionManager geoDefinitionManager;
  private final Map<String, NMEAMessageTranslator> translators;
  private String nmeaDataField;
  private Map<String, GeoEventDefinition> edMapper = new ConcurrentHashMap<>();

  public NMEADecoder(GeoEventProcessorDefinition definition, GeoEventCreator geoEventCreator, GeoEventDefinitionManager geoDefinitionManager, Map<String, NMEAMessageTranslator> translators) throws ComponentException {
    super(definition);
    this.geoEventCreator = geoEventCreator;
    this.geoDefinitionManager = geoDefinitionManager;
    this.translators = translators;
  }

  @Override
  public boolean isGeoEventMutator() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    if (hasProperty("nmeaDataField")) {
      nmeaDataField = getProperty("nmeaDataField").getValueAsString();
    }
  }

  @Override
  public GeoEvent process(GeoEvent inEvent) throws Exception {

    // check if mandatory NMEA data field name is defined
    if (nmeaDataField == null || inEvent.getField(nmeaDataField) == null) {
      LOG.debug(String.format("Unable to process event"));
      return null;
    }

    // get content of the NMEA data field from the event
    String nmeaData = StringUtils.trimToEmpty(inEvent.getField(nmeaDataField).toString());
    String[] elements = nmeaData!=null? nmeaData.split(","): null;

    if (elements == null || elements.length == 0) {
      LOG.debug(String.format("Invalid NMEA data: %s", nmeaData));
      return null;
    }

    // get matching translator
    String type = elements[0].replaceAll("^\\$", "");
    GeoEventDefinition nmeaEventDefinition = ((NMEADecoderDefinition) definition).getGeoEventDefinition(type);
    NMEAMessageTranslator nmeaTranslator = translators.get(type);

    if (nmeaTranslator == null || nmeaEventDefinition==null) {
      LOG.debug(String.format("Unsupported NMEA type: %s", type));
      return null;
    }

    try {
      nmeaTranslator.validate(elements);
    } catch (ValidationException ex) {
      LOG.debug(String.format("Invalid NMEA data: %s", nmeaData), ex);
      return null;
    }
    
    // find or produce output geo-event definition
    GeoEventDefinition inEventDef = inEvent.getGeoEventDefinition();
    String outEventKey = String.format("%s/%s", inEventDef.getGuid(), nmeaEventDefinition.getGuid());
    GeoEventDefinition outEventDef = edMapper.containsKey(outEventKey)? edMapper.get(outEventKey): null;
    if (outEventDef==null) {
      outEventDef = nmeaEventDefinition.augment(inEventDef.getFieldDefinitions());
      registerDefinition(outEventKey, outEventDef);
    }
    
    // create new geo-event
    GeoEvent outEvent = geoEventCreator.create(outEventDef.getGuid());
    nmeaTranslator.translate(outEvent, elements);
    for (FieldDefinition fd: inEvent.getGeoEventDefinition().getFieldDefinitions()) {
      outEvent.setField(fd.getName(), inEvent.getField(fd.getName()));
    }

    return outEvent;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    unregisterAllDefinitions();
  }
  
  private void registerDefinition(String outEventKey, GeoEventDefinition outEventDef) throws GeoEventDefinitionManagerException {
    geoDefinitionManager.addTemporaryGeoEventDefinition(outEventDef, true);
    edMapper.put(outEventKey, outEventDef);
  }
  
  private void unregisterAllDefinitions() {
    edMapper.values().stream().forEach(eventDef->{
      try {
        geoDefinitionManager.deleteGeoEventDefinition(eventDef.getGuid());
      } catch (GeoEventDefinitionManagerException ex) {
        LOG.warn(String.format("Failed deleting geo-event definition: %s", eventDef.getGuid()), ex);
      }
    });
    edMapper.clear();
  }
}
