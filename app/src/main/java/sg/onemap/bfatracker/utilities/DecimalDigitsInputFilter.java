package sg.onemap.bfatracker.utilities;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {

    private final int digitsBeforeZero;
    private final int digitsAfterZero;
    private Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
        this.digitsBeforeZero = digitsBeforeZero;
        this.digitsAfterZero = digitsAfterZero;
        applyPattern(digitsBeforeZero, digitsAfterZero);
    }

    private void applyPattern(int digitsBeforeZero, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)|(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (dest.toString().contains(".") || source.toString().contains("."))
            applyPattern(digitsBeforeZero + 2, digitsAfterZero);
        else
            applyPattern(digitsBeforeZero, digitsAfterZero);

        Matcher matcher = mPattern.matcher(dest);
        if (!matcher.matches())
            return "";
        return null;
    }

}