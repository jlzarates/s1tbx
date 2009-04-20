package org.esa.beam.glayer;

import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerContext;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.binding.ValueContainer;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelSource;
import org.esa.beam.glevel.BandImageMultiLevelSource;
import org.esa.beam.framework.datamodel.RasterDataNode;

public class RasterImageLayerType extends LayerType {

    public static final String PROPERTY_NAME_RASTER = "raster";

    @Override
    public String getName() {
        return "Raster Data Layer";
    }

    @Override
    public boolean isValidFor(LayerContext ctx) {
        return true;
    }

    @Override
    public ImageLayer createLayer(LayerContext ctx, ValueContainer configuration) {
        final RasterDataNode raster = (RasterDataNode) configuration.getValue(PROPERTY_NAME_RASTER);
        final MultiLevelSource multiLevelSource = BandImageMultiLevelSource.create(raster, ProgressMonitor.NULL);
        return new ImageLayer(this, configuration, multiLevelSource);
    }

    @Override
    public ValueContainer getConfigurationTemplate() {
        final ValueContainer template = new ValueContainer();

        template.addModel(createDefaultValueModel(PROPERTY_NAME_RASTER, RasterDataNode.class));
        template.getDescriptor(PROPERTY_NAME_RASTER).setNotNull(true);

        template.addModel(createDefaultValueModel(ImageLayer.PROPERTY_NAME_BORDER_SHOWN,
                                                  ImageLayer.DEFAULT_BORDER_SHOWN));
        template.getDescriptor(ImageLayer.PROPERTY_NAME_BORDER_SHOWN).setDefaultValue(ImageLayer.DEFAULT_BORDER_SHOWN);

        template.addModel(createDefaultValueModel(ImageLayer.PROPERTY_NAME_BORDER_COLOR,
                                                  ImageLayer.DEFAULT_BORDER_COLOR));
        template.getDescriptor(ImageLayer.PROPERTY_NAME_BORDER_COLOR).setDefaultValue(ImageLayer.DEFAULT_BORDER_COLOR);

        template.addModel(createDefaultValueModel(ImageLayer.PROPERTY_NAME_BORDER_WIDTH,
                                                  ImageLayer.DEFAULT_BORDER_WIDTH));
        template.getDescriptor(ImageLayer.PROPERTY_NAME_BORDER_WIDTH).setDefaultValue(ImageLayer.DEFAULT_BORDER_WIDTH);

        return template;
    }

    public ImageLayer createLayer(RasterDataNode raster) {
        final ValueContainer configuration = getConfigurationTemplate();

        try {
            configuration.setValue(PROPERTY_NAME_RASTER, raster);
        } catch (ValidationException e) {
            throw new IllegalArgumentException(e);
        }

        return createLayer(null, configuration);
    }
}
