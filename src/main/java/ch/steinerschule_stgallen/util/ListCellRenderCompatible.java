package ch.steinerschule_stgallen.util;

public interface ListCellRenderCompatible {
    public double getTotal(Currency cur);
    public double getTotalCombinedCHF();
    public double getTotalCombinedCHFOpen();
    public double getOpenTotal(Currency cur);
    public String getNameCellRender();
}
