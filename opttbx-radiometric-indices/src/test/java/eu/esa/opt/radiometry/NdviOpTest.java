package eu.esa.opt.radiometry;

import java.util.HashMap;

public class NdviOpTest extends BaseIndexOpTest<NdviOp> {

    @Override
    public void setUp() throws Exception {
        setupBands(new String[] { "RED", "NIR" }, 4, 1, new float[] { 650, 850 },
                   new float[] { 0, 1, 2, 3 }, new float[] { 1, 2, 3, 0 });
        setOperatorParameters(new HashMap<String, Float>() {{
            put("redFactor", 1.0f);
            put("nirFactor", 1.0f);
        }});
        setTargetValues(new float[] { 1.0f, 0.33333334f, 0.2f, -1.0f } );
        super.setUp();
    }

}
