package io.github.palexdev.imcache.transforms;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/// The [GaussianBlur] class applies a Gaussian blur filter to a given [BufferedImage]. The transformation uses a
/// convolution operation with a pre-defined Gaussian kernel.
///
/// The kernel used in this implementation is a 5x5 matrix that approximates the Gaussian distribution.
public class GaussianBlur implements Transform {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        float[] matrix = {
            1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256,
            4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
            6f / 256, 24f / 256, 36f / 256, 24f / 256, 6f / 256,
            4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
            1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256
        };
        Kernel kernel = new Kernel(5, 5, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(src, null);
    }
}
