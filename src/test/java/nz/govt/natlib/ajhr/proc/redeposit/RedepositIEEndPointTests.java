package nz.govt.natlib.ajhr.proc.redeposit;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;

public class RedepositIEEndPointTests {
    @Test
    public void testLoadProperties() throws IOException {
        String propFileName = "unpublished";
        RedepositIEEndPoint instance = RedepositIEEndPoint.getInstance(propFileName);

        assert StringUtils.isNotBlank(instance.getSrcDir());
        assert StringUtils.isNotBlank(instance.getDestDir());
    }
}
