/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;

public class GaussianSamplerFromParams extends ScalarProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KeyValueParamsProvider keyValueParamsProvider;

    @BeanField @Getter @Setter private String paramMean = "";

    @BeanField @Getter @Setter private String paramStdDev = "";

    @BeanField @Getter @Setter
    private double factorStdDev = 1.0; // Multiples the standard deviation by a factor
    // END BEAN PROPERTIES

    @Override
    public double propose(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws OperationFailedException {

        try {
            KeyValueParams kvp = keyValueParamsProvider.create();
            assert (kvp != null);

            if (!kvp.containsKey(getParamMean())) {
                throw new OperationFailedException(
                        String.format("Params are missing key '%s' for paramMean", getParamMean()));
            }

            if (!kvp.containsKey(getParamStdDev())) {
                throw new OperationFailedException(
                        String.format(
                                "Params are missing key '%s' for paramStdDev", getParamStdDev()));
            }

            double mean = Double.parseDouble(kvp.getProperty(getParamMean()));
            double sd = Double.valueOf(kvp.getProperty(getParamStdDev())) * factorStdDev;

            return randomNumberGenerator.generateNormal(mean, sd).nextDouble();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
