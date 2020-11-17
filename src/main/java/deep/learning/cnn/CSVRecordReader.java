package deep.learning.cnn;

import java.util.List;

public class CSVRecordReader extends org.datavec.api.records.reader.impl.csv.CSVRecordReader{
	
	
    public CSVRecordReader(int skipNumLines, char delimiter){
        super(skipNumLines, delimiter);
    }
    
    List<String> labels = null;
	
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
    @Override
    public List<String> getLabels() {
    	return labels;
    }
	

}
