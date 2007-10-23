/*
 * $Id: ProductMergeOp.java,v 1.3 2007/05/14 12:25:40 marcoz Exp $
 *
 * Copyright (C) 2007 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.framework.gpf.operators.common;

import java.awt.image.RenderedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.StringUtils;

import com.bc.ceres.core.ProgressMonitor;

@OperatorMetadata(alias = "ProductMerger")
public class ProductMergeOp extends Operator {

    @Parameter(defaultValue = "UNKNOWN", description="The product type for the target product.")
    private String productType;
    @Parameter(description="The ID of the source product providing the geo-coding.", alias="baseGeoInfo")
//    private String baseGeoInfo;
    private String copyGeoCodingFrom;
    @Parameter(itemAlias = "band", itemsInlined = true)
    private BandDesc[] bands;


    @Override
    public Product initialize() throws OperatorException {

        Product outputProduct;
        if (StringUtils.isNotNullAndNotEmpty(copyGeoCodingFrom)) {
            Product baseGeoProduct = getSourceProduct(copyGeoCodingFrom);
            final int sceneRasterWidth = baseGeoProduct.getSceneRasterWidth();
            final int sceneRasterHeight = baseGeoProduct.getSceneRasterHeight();
            outputProduct = new Product("mergedName", productType,
                                        sceneRasterWidth, sceneRasterHeight);

            copyGeoCoding(baseGeoProduct, outputProduct);
        } else {
            BandDesc bandDesc = bands[0];
            Product srcProduct = getSourceProduct(bandDesc.product);
            final int sceneRasterWidth = srcProduct.getSceneRasterWidth();
            final int sceneRasterHeight = srcProduct.getSceneRasterHeight();
            outputProduct = new Product("mergedName", productType,
                                        sceneRasterWidth, sceneRasterHeight);
        }

        Set<Product> allSrcProducts = new HashSet<Product>();
        for (BandDesc bandDesc : bands) {
            Product srcProduct = getSourceProduct(bandDesc.product);
            if (StringUtils.isNotNullAndNotEmpty(bandDesc.name)) {
                if (StringUtils.isNotNullAndNotEmpty(bandDesc.newName)) {
                    copyBandWithFeatures(srcProduct, outputProduct, bandDesc.name, bandDesc.newName);
                } else {
                    copyBandWithFeatures(srcProduct, outputProduct, bandDesc.name);
                }
                allSrcProducts.add(srcProduct);
            } else if (StringUtils.isNotNullAndNotEmpty(bandDesc.nameExp)) {
                Pattern pattern = Pattern.compile(bandDesc.nameExp);
                for (String bandName : srcProduct.getBandNames()) {
                    Matcher matcher = pattern.matcher(bandName);
                    if (matcher.matches()) {
                        copyBandWithFeatures(srcProduct, outputProduct, bandName);
                        allSrcProducts.add(srcProduct);
                    }
                }
            }
        }

        for (Product srcProduct : allSrcProducts) {
            ProductUtils.copyBitmaskDefsAndOverlays(srcProduct, outputProduct);
        }

        return outputProduct;
    }

    /*
     * Copies the tie point data, geocoding and the start and stop time.
     */
    private static void copyGeoCoding(Product sourceProduct,
                                        Product destinationProduct) {
        // copy all tie point grids to output product
        ProductUtils.copyTiePointGrids(sourceProduct, destinationProduct);
        // copy geo-coding to the output product
        ProductUtils.copyGeoCoding(sourceProduct, destinationProduct);
        destinationProduct.setStartTime(sourceProduct.getStartTime());
        destinationProduct.setEndTime(sourceProduct.getEndTime());
    }

    private void copyBandWithFeatures(Product srcProduct, Product outputProduct, String oldBandName, String newBandName) {
        Band destBand = copyBandWithFeatures(srcProduct, outputProduct, oldBandName);
        destBand.setName(newBandName);
    }

    private Band copyBandWithFeatures(Product srcProduct, Product outputProduct, String bandName) {
        Band destBand = ProductUtils.copyBand(bandName, srcProduct, outputProduct);
        Band srcBand = srcProduct.getBand(bandName);
        RenderedImage image = srcBand.getImage();
        if (image != null) {
            destBand.setImage(image);
        }
        if (srcBand.getFlagCoding() != null) {
            FlagCoding srcFlagCoding = srcBand.getFlagCoding();
            ProductUtils.copyFlagCoding(srcFlagCoding, outputProduct);
            destBand.setFlagCoding(outputProduct.getFlagCoding(srcFlagCoding.getName()));
        }
        return destBand;
    }

    @Override
    public void computeTile(Band band, Tile targetTile, ProgressMonitor pm) throws OperatorException {
        getLogger().warning("Wrongly configured ProductMerger operator. Tiles should not be requested.");
    }

    public static class BandDesc {
        String product;
        String name;
        String nameExp;
        String newName;
    }


    public static class Spi extends OperatorSpi {
        public Spi() {
            super(ProductMergeOp.class);
        }
    }
}