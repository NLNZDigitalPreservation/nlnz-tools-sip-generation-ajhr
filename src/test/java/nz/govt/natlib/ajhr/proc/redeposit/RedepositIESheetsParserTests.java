package nz.govt.natlib.ajhr.proc.redeposit;

import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RedepositIESheetsParserTests {
    @Test
    public void testParse() {
        parse("UnpublishedIE", false);
        parse("UnpublishedIE-Multifile", true);
        parse("DigitisedImageIE", false);
        parse("OneoffIE", false);
        parse("PeriodicIE", false);
    }

    private void parse(String sheetName, boolean isMultipleRowsExtension) {
        try {
            List<RedepositIeDTO> rstVal = RedepositIESheetsParser.parse(sheetName, isMultipleRowsExtension);
            assert rstVal.size() > 0;

            System.out.println("=============" + sheetName + "===========");
            RedepositIeDTO dto = rstVal.get(0);
            System.out.println(dto.getEventDateTime());
            System.out.println(dto.getIeCreationDate());

            assert true;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
}
