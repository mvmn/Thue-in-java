package x.mvmn.misc.thue.interpreter.events;

public interface IThueInterpreterDataChangeListener {
	public void dataChanged(String data, int idxStart, int idxEnd);
}
