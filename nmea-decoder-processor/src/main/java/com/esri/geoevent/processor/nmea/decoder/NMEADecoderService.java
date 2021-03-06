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

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import java.util.Map;

public class NMEADecoderService extends GeoEventProcessorServiceBase {
	private final GeoEventCreator	geoEventCreator;
  private final GeoEventDefinitionManager geoDefinitionManager;
  private final Map<String,NMEAMessageTranslator> translators;

  public NMEADecoderService(Messaging messaging, GeoEventDefinitionManager geoDefinitionManager, Map<String,NMEAMessageTranslator> translators) throws PropertyException {
    this.definition = new NMEADecoderDefinition(translators);
    this.geoEventCreator = messaging.createGeoEventCreator();
    this.geoDefinitionManager = geoDefinitionManager;
    this.translators = translators;
  }

  @Override
  public GeoEventProcessor create() throws ComponentException {
    return new NMEADecoder(definition, geoEventCreator, geoDefinitionManager, translators);
  }
}
