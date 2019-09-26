package com.example.hw1_calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    public boolean error;
    public Queue<String> postfix;
    public Stack<String> operators;
    public Stack<Double> operands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.number)).setText("0"); //수식 입력창의 초기값을 0으로 설정
        error = false; //0으로 나누기를 시도하는 경우 혹은 온전하지 않은 계산식을 계산 시도했을 경우 true로 설정됨
    }

    public void numClicked(View v) {
        String buttonText = ((Button) v).getText().toString(); //이 함수를 호출한 버튼(사용자에 의해 눌린 버튼)의 텍스트 정보(숫자)를 가져옴 : 1이 눌렸을 경우 "1", 2가 눌렸을 경우"2" 등...
        TextView t = (TextView) findViewById(R.id.number); //수식 입력창을 제어하기 위함.
        if (error) {//이전에 0으로 나누는 등 에러가 발생했을 경우 그에 대한 처리를 해줌
            t.setText("0");
            error = false;
        }
        String lineText = t.getText().toString();//수식창에 이미 적혀있던 내용을 가져옴

        if (Character.toString(lineText.charAt(lineText.length() - 1)).equals("0")) {//숫자가 0이었을 경우, 사용자가 다른 숫자를 눌렀을 때, 00 혹은 05 등의 형태가 되지 않도록 기존에 존재하던 0을 지워줌
            if (lineText.length() < 2 || isOperator(lineText.charAt(lineText.length() - 2))) {
                lineText = lineText.substring(0, lineText.length() - 1);
            }
        }
        t.setText(lineText + buttonText);//수식창의 기존 내용에 사용자가 누른 버튼을 추가하여 디스플레이
    }

    public void operatorClicked(View v) {//연산자 버튼이 눌렸을 때 호출됨
        String buttonText = ((Button) v).getText().toString();
        TextView t = (TextView) findViewById(R.id.number);
        if (error) {//이전에 0으로 나누는 등 에러가 발생했을 경우 그에 대한 처리를 해줌
            t.setText("0");
            error = false;
        }
        String lineText = t.getText().toString();

        if (isOperator(lineText.charAt(lineText.length() - 1))) {//사용자가 연산자를 여러 번 연속해서 누르는 경우를 대비하는 코드
            lineText = lineText.substring(0, lineText.length() - 1);
        }
        t.setText(lineText + buttonText);//기존 수식에 사용자가 누른 연산자를 추가하여 디스플레이
    }

    public void clearClicked(View v) {//수식과 상태변수를 초기화
        error = false;
        ((TextView) findViewById(R.id.number)).setText("0");
    }

    public void okClicked(View v) {
        TextView t = (TextView) findViewById(R.id.number);
        Double result;
        String resultText;
        if (error) {//이전에 0으로 나누는 등 에러가 발생했을 경우 그에 대한 처리를 해줌
            t.setText("0");
            error = false;
        }
        String toSolve = t.getText().toString();//수식창에 있던 수식을 가져옴

        try {
            result = postfixCalculator(postfixTranslator(toSolve));//수식을 계산 (함수 설명은 아래에)
            if (result == 0) result = 0.0;// Java Double::toString()이 -0.0 과 0.0을 구분하기에 -0.0을 표시하지 않기 위한 코드
            resultText = result.toString();
            t.setText((resultText.substring(resultText.length() - 2).equals(".0")) ? resultText.substring(0, resultText.length() - 2) : resultText);//숫자 뒤에 .0 형태의 꼬리를 제거하기 위한 코드
        } catch (ArithmeticException e) {//0으로 나누기를 시도할 시 발생하는 예외를 처리
            error = true;
            t.setText("You can't divide by 0");
        } catch (NumberFormatException e) {//수식이 완전하지 않은 경우의 예외를 처리
            error = true;
            t.setText("Format error");
        }
    }

    public boolean isOperator(char input) {//문자가 연산자인지 구분해 주는 함수
        return (input == '+' || input == '-' || input == '*' || input == '/');
    }

    public Queue<String> postfixTranslator(String toSolve) {//중위표현식을 후위표현식으로 변환한다
        postfix = new LinkedList<String>();
        operators = new Stack<String>();
        boolean startWithMinus = false;
        char current;
        String stackTop;
        if (toSolve.charAt(0) == '-') {//수식이 음수로 시작하는 경우, 음수 -가 아닌 빼기-로 인식하여 이를 해결하기 위한 코드
            startWithMinus = true;
            toSolve = toSolve.substring(1);
        }
        for (int i = 0; i < toSolve.length(); ++i) {
            current = toSolve.charAt(i);
            if (isOperator(current)) {//수식을 검사하면서 연산자를 만난 경우
                if (startWithMinus) {//수식이 음수로 시작했던 경우를 숫자 앞에 -를 추가하여 하나의 스트링으로 큐에 넣는다
                    startWithMinus = false;
                    postfix.offer('-' + toSolve.substring(0, i));
                } else
                    postfix.offer(toSolve.substring(0, i));//연산자 바로 앞까지가 한 숫자뭉치이므로 하나의 스트링으로 큐에 넣는다
                if (!operators.empty()) {//기존에 스택에 연산자들이 저장되어있던 경우 현재 연산자보다 우선순위가 높은 연산자들을 모두 스택에서 빼서 큐에 넣어준다
                    if (current == '+' || current == '-') {// 현재 연산자가 +혹은 -인 경우, 스택에 있는 모든 연산자보다 우선순위가 낮다
                        while (!operators.empty())
                            postfix.offer(operators.pop());
                    } else {
                        stackTop = operators.peek();
                        while (stackTop.equals("*") || stackTop.equals("/")) {// 현재 연산자가 *혹은 /인 경우, 스택에 있는 *혹은 /만을 빼서 큐에 넣어준다
                            postfix.offer(operators.pop());
                            if (!operators.isEmpty())
                                stackTop = operators.peek();
                            else
                                break;
                        }
                    }
                }
                operators.push(Character.toString(current));//현재 연산자를 스택에 넣어준다
                toSolve = toSolve.substring(i + 1);//처리한 숫자와 연산자를 수식에서 빼 준다
                i = 0;//수식에서 이미 처리한 부분을 뺏으므로, 다시 수식의 처음부터 처리하기 위함
            }
        }
        postfix.offer(toSolve);//수식의 끝자락에 연산자가 없으므로 마지막 숫자는 처리가 안 되기에 그것을 큐에 넣어준다
        while (!operators.isEmpty())//스택에 남은 연산자들을 모조리 큐에 넣어준다
            postfix.offer(operators.pop());
        return postfix;//완성된 후위표현식 반환
    }

    public Double postfixCalculator(Queue<String> toSolve) {//후위표현식을 계산하여 그 값을 반환한다
        Double a, b;
        String current;
        operands = new Stack<Double>();
        while (!toSolve.isEmpty()) {//수식의 모든 항을 처리할 때까지 돈다
            current = toSolve.poll();//큐의 첫 번째 원소를 가져온다
            if (current.equals("+")) {//스택에서 두 개의 원소를 가져와 더해준 뒤, 결과값을 스택에 넣어준다.
                b = operands.pop();
                a = operands.pop();
                operands.push(a + b);
            } else if (current.equals("-")) {
                b = operands.pop();
                a = operands.pop();
                operands.push(a - b);
            } else if (current.equals("*")) {
                b = operands.pop();
                a = operands.pop();
                operands.push(a * b);
            } else if (current.equals("/")) {
                b = operands.pop();
                a = operands.pop();
                if (b.equals(0.0)) throw new ArithmeticException();//0으로 나누기를 시도할 경우 사용자 정의 예외를 발생시킨다
                operands.push(a / b);
            } else
                operands.push(Double.parseDouble(current));//원소가 연산자가 아닌 숫자인 경우 스택에 넣어준다.
        }
        if (operands.size() != 1) throw new NumberFormatException();//연산자가 없는데 피연산자가 2개 이상 남은경우 수식오류이므로 사용자 정의 예외를 발생시킨다
        return operands.pop();//계산의 최종 결과값을 반환
    }
}
