package edu.gatech.cc.cs3251.ateichner3.edu.gatech.cc.cs3251.ateichner3.project1;

public class rpnProtocol {

    public rpnProtocol () {

    }
    private static final int WAITING = 0;
    private static final int SENTRESULT = 1;
    private static final int SENTERROR = 2;
    private static final int ANOTHER = 3;

    private int state = WAITING;

    public String evaluateRpn(String[] rpn) {
        switch (state) {
            case WAITING:
                return calculate_result(rpn);
            case ANOTHER:
                return calculate_result(rpn);
            case SENTRESULT:
                state = WAITING;
                break;
            case SENTERROR:
                state = WAITING;
                break;
            default:
                state = WAITING;
                break;
        }
        return "WAITING";
    }

    /**
     * calculate_result() takes in a String[] formatted using the specifications below and performs a calculation.
     * The protocol state machine is then updated to reflect the current state,
     * @param rpn the String[] containing the component parts of the query
     *            rpn[0]: the first operand (integer 1)
     *            rpn[1]: the operator (addition, subtraction, etc)
     *            rpn[2]: the second operand (integer 2)
     *            rpn[3]: either the String "ANOTHER" (all-caps required) to signify another or "END" to signify end
     * @return currentResult the result of the operation
     */
    private String calculate_result(String[] rpn) {
        int currentResult = 0;
        if (rpn.length == 4) {
            switch (rpn[1]) {
                case "+":
                    currentResult = Integer.parseInt(rpn[0]) + Integer.parseInt(rpn[2]);
                    break;
                case "-":
                    currentResult = Integer.parseInt(rpn[0]) - Integer.parseInt(rpn[2]);
                    break;
                case "*":
                    currentResult = Integer.parseInt(rpn[0]) * Integer.parseInt(rpn[2]);
                    break;
                case "/":
                    currentResult = Integer.parseInt(rpn[0]) / Integer.parseInt(rpn[2]);
                    break;
                case "x":
                    currentResult = Integer.parseInt(rpn[0]) / Integer.parseInt(rpn[2]);
                    break;
                case "X":
                    currentResult = Integer.parseInt(rpn[0]) * Integer.parseInt(rpn[2]);
                    break;
                default:
                    state = SENTERROR;
                    String currentResultStr = Integer.toString(currentResult);
                    return (currentResultStr + ", ERROR: INVALID OPERATOR, PROTOCOL NOT FOLLOWED");
            }
            switch (rpn[3]) {
                case "ANOTHER": {
                    state = ANOTHER;
                    String currentResultStr = Integer.toString(currentResult);
                    return (currentResultStr + ", OK ANOTHER");
                }
                case "END": {
                    state = SENTRESULT;
                    String currentResultStr = Integer.toString(currentResult);
                    return (currentResultStr + ", OK END");
                }
                default: {
                    currentResult = 0;
                    state = SENTERROR;
                    String currentResultStr = Integer.toString(currentResult);
                    return (currentResultStr + ", ERROR: INVALID NEXT STATE INSTRUCTION, PROTOCOL NOT FOLLOWED");
                }
            }
        } else {
            state = SENTERROR;
            String currentResultStr = Integer.toString(currentResult);
            return (currentResultStr + ", " + "ERROR: INDEX OUT OF BOUNDS, PROTOCOL NOT FOLLOWED.");
        }
    }
}
