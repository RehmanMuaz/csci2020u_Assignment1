package assignment1;

import java.text.DecimalFormat;

public class TestFile {

  private String fileName;
  private double spamProbability;
  private String actualClass;
  private String validResult;

  public TestFile(String fileName, double spamProbability, String actualClass, String validResult) {
    this.fileName = fileName;
    this.spamProbability = spamProbability;
    this.actualClass = actualClass;
    this.validResult = validResult;
  }

  public String getFileName() {
    return this.fileName;
  }

  public double getSpamProbability() {
    return this.spamProbability;
  }

  public String getSpamProbRounded() {
    DecimalFormat df = new DecimalFormat("0.00000");
    return df.format(this.spamProbability);
  }

  public void setValidResult(String value) {
    this.validResult = value;
  }

  public String getValidResult() {
    return this.validResult;
  }

  public String getActualClass() {
    return this.actualClass;
  }

  public void setFileName(String value) {
    this.fileName = value;
  }

  public void setSpamProbability(double val) {
    this.spamProbability = val;
  }

  public void setActualClass(String value) {
    this.actualClass = value;
  }
}
