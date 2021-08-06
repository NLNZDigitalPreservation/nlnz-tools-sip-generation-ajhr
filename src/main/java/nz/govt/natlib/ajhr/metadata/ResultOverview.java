package nz.govt.natlib.ajhr.metadata;

import nz.govt.natlib.ajhr.util.PrettyPrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultOverview {
    private final List<ResultItem> allFinished = new ArrayList<>();

    public void addResultItem(MetadataRetVal result, File subFolder) {
        ResultItem resultItem = new ResultItem(result, subFolder);
        allFinished.add(resultItem);
    }

    public void clear() {
        allFinished.clear();
    }

    public String getSummaryInfo() {
        final StringBuffer bufInto = new StringBuffer();
        bufInto.append("=========================================================================================").append(System.lineSeparator());

        Map<String, List<ResultItem>> groupedByParentFolder = allFinished.stream().collect(Collectors.groupingBy(ResultItem::getParentFolder));
        groupedByParentFolder.forEach((k, v) -> {
            bufInto.append(k).append(System.lineSeparator());
            v.forEach(item -> {
                String msg = String.format("\t\t----[%s] %s", item.getResult().name(), item.getSubFolder().getName());
                String color;

                if (item.getResult() == MetadataRetVal.SUCC) {
                    color = PrettyPrinter.ANSI_GREEN;
                } else if (item.getResult() == MetadataRetVal.SKIP) {
                    color = PrettyPrinter.ANSI_CYAN;
                } else {
                    color = PrettyPrinter.ANSI_RED;
                }

                bufInto.append(color).append(msg).append(System.lineSeparator());
            });
        });
        groupedByParentFolder.forEach((k, v) -> {
            v.clear();
        });
        groupedByParentFolder.clear();

        Map<MetadataRetVal, List<ResultItem>> groupedByResult = allFinished.stream().collect(Collectors.groupingBy(ResultItem::getResult));
        for (MetadataRetVal val : MetadataRetVal.values()) {
            if (!groupedByResult.containsKey(val)) {
                groupedByResult.put(val, new ArrayList<>());
            }
        }
        bufInto.append(PrettyPrinter.ANSI_RESET);
        bufInto.append(String.format("There are [%d] SIPs processed", allFinished.size()));
        groupedByResult.forEach((k, v) -> {
            bufInto.append(String.format(", [%s: %d]", k.name(), v.size()));
        });
        groupedByResult.forEach((k, v) -> {
            v.clear();
        });
        groupedByResult.clear();

        return bufInto.toString();
    }

    static class ResultItem {
        private MetadataRetVal result;
        private File subFolder;

        public ResultItem(MetadataRetVal result, File subFolder) {
            this.result = result;
            this.subFolder = subFolder;
        }

        public String getParentFolder() {
            return subFolder.getParentFile().getAbsolutePath();
        }

        public File getSubFolder() {
            return subFolder;
        }

        public void setSubFolder(File subFolder) {
            this.subFolder = subFolder;
        }

        public MetadataRetVal getResult() {
            return result;
        }

        public void setResult(MetadataRetVal result) {
            this.result = result;
        }
    }
}
