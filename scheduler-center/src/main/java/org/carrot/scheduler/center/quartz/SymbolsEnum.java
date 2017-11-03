package org.carrot.scheduler.center.quartz;

import java.util.Calendar;

public enum SymbolsEnum {
    SECOND("s", Calendar.SECOND),
    MINUTE("m", Calendar.MINUTE),
    HOUR("h", Calendar.HOUR),
    DAY("D", Calendar.DATE),
    WEEK("W", Calendar.WEEK_OF_YEAR),
    MONTH("M", Calendar.MONTH),
    YEAR("Y", Calendar.YEAR);
    private String symbol;
    private int field;

    SymbolsEnum(String symbol, int field) {
        this.symbol = symbol;
        this.field = field;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public static SymbolsEnum getBySymbols(String symbol){
        SymbolsEnum[]  symbolsEnums = SymbolsEnum.values();
        for (SymbolsEnum symbolsEnum: symbolsEnums){
            if(symbolsEnum.symbol.equals(symbol)){
                return symbolsEnum;
            }
        }
        return null;
    }

}
