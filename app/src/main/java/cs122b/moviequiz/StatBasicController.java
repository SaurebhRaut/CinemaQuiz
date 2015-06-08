package cs122b.moviequiz;

import android.view.View;

public class StatBasicController extends Basic_Controller {
    public StatBasicController() {
        super(R.layout.result);
    }

    @Override
    protected void onShow() {
        getButton(R.id.statsBackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        QuizStat results = getActivity().getResults();
        getTextView(R.id.statsNumQuizes).setText(String.valueOf(results.QuizCount()));
        getTextView(R.id.statsQuestionsAnswered).setText(String.valueOf(results.QuestionsTaken()));
        getTextView(R.id.statsCorrectAnswers).setText(String.valueOf(results.CorrectAnswers()));
        getTextView(R.id.statsWrongAnswers).setText(String.valueOf(results.WrongAnswers()));
        getTextView(R.id.statsAverageTime).setText(SecondsCalculation(results.AvgQuizTime()));

    }

    private String SecondsCalculation(int result){
        String seconds =  "" + result/1000;
        String Millisec = "" + result%1000;
        while(Millisec.length()<4){
            Millisec = "0" + Millisec;
        }
        while(Millisec.length()>1&&Millisec.charAt(Millisec.length()-1)=='0'){
            Millisec = Millisec.substring(0, Millisec.length()-1);
        }
        return seconds + "." + Millisec;
    }

    @Override
    protected void onHide() {

    }
}
