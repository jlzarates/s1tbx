/*
 * $Id: $
 *
 * Copyright (C) 2008 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.framework.gpf.graph;

import com.bc.ceres.binding.Converter;
import com.bc.ceres.binding.Validator;
import com.bc.ceres.binding.dom.DomConverter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class HeaderParameter {
    private String name;
    private String type;
    private String defaultValue;
    private String description;
    private String label;
    private String unit;
    private String interval;
    private String[] valueSet;
    private String condition;
    private String pattern;
    private String format;
    private boolean notNull;
    private boolean notEmpty;
    private Class<? extends Validator> validator;
    private Class<? extends Converter> converter;
    private Class<? extends DomConverter> domConverter;
    private String itemAlias;
    private boolean itemsInlined;
    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public String getInterval() {
        return interval;
    }
    public void setInterval(String interval) {
        this.interval = interval;
    }
    public String[] getValueSet() {
        return valueSet;
    }
    public void setValueSet(String[] valueSet) {
        this.valueSet = valueSet;
    }
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
    public String getPattern() {
        return pattern;
    }
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public boolean isNotNull() {
        return notNull;
    }
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }
    public boolean isNotEmpty() {
        return notEmpty;
    }
    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }
    public Class<? extends Validator> getValidator() {
        return validator;
    }
    public void setValidator(Class<? extends Validator> validator) {
        this.validator = validator;
    }
    public Class<? extends Converter> getConverter() {
        return converter;
    }
    public void setConverter(Class<? extends Converter> converter) {
        this.converter = converter;
    }
    public Class<? extends DomConverter> getDomConverter() {
        return domConverter;
    }
    public void setDomConverter(Class<? extends DomConverter> domConverter) {
        this.domConverter = domConverter;
    }
    public String getItemAlias() {
        return itemAlias;
    }
    public void setItemAlias(String itemAlias) {
        this.itemAlias = itemAlias;
    }
    public boolean isItemsInlined() {
        return itemsInlined;
    }
    public void setItemsInlined(boolean itemsInlined) {
        this.itemsInlined = itemsInlined;
    }
    
    public static class Converter implements com.thoughtworks.xstream.converters.Converter {

        public boolean canConvert(Class aClass) {
            return HeaderParameter.class.equals(aClass);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer,
                MarshallingContext context) {
            HeaderParameter headerParameter = (HeaderParameter) source;
            writer.addAttribute("name", headerParameter.getName());
            writer.addAttribute("type", headerParameter.getType());
            writer.addAttribute("defaultValue", headerParameter.getDefaultValue());
            writer.addAttribute("description", headerParameter.getDescription());
            writer.addAttribute("label", headerParameter.getLabel());
            writer.addAttribute("unit", headerParameter.getUnit());
            writer.addAttribute("interval", headerParameter.getInterval());
            writer.addAttribute("interval", headerParameter.getInterval());
//            writer.addAttribute("optional", Boolean
//                    .toString(headerSource.optional));
//            writer.setValue(headerSource.getLocation());

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader,
                UnmarshallingContext context) {
//            HeaderSource headerSource = new HeaderSource();
//
//            headerSource.name = reader.getAttribute("name");
//            headerSource.description = reader.getAttribute("description");
//            headerSource.optional = Boolean.parseBoolean(reader
//                    .getAttribute("optional"));
//            headerSource.value = reader.getValue();
//            return headerSource;
            return null;
        }
    }
    
}
