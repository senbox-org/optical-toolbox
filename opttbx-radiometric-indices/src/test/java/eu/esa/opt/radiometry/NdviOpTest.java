package eu.esa.opt.radiometry;

import org.junit.Before;

import java.util.HashMap;

public class NdviOpTest extends BaseIndexOpTest<NdviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"RED", "NIR"}, 3, 3, new float[]{650, 850},
                new float[]{0, 1}, new float[]{9, 10});
        setOperatorParameters(new HashMap<String, Float>() {{
            put("redFactor", 1.0f);
            put("nirFactor", 1.0f);
        }});
        setTargetValues(new float[]{
                1.0f, 0.30769232f, 0.18181819f,
                0.12903225f, 0.1f, 0.08163265f,
                0.06896552f, 0.05970149f, 0.05263158f});
    }
}
