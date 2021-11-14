package vn.edu.tdc.zuke_customer.data_models;

public class TheCustomerConslutant {
    private String question;
    private String answer;

    public TheCustomerConslutant(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
