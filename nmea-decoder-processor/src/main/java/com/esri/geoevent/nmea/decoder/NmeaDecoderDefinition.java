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
package com.esri.geoevent.nmea.decoder;

import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

/**
 * NMEA decoder definition.
 */
public class NmeaDecoderDefinition extends GeoEventProcessorDefinitionBase {

	@Override
	public String getName()
	{
		return "NmeaDecoder";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.nmea-decoder";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.nmea-decoder.DECODER_LBL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.nmea-decoder.DECODER_DESC}";
	}

	@Override
	public String getVersion()
	{
		return "10.6.1";
	}}
