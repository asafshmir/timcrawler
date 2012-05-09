package timc.common;

public class Utils  {
	
	public enum OperationMode {
		Normal(0),
		NeverNotifySeeder(1),
		HalfSeedDropNewPieces(2);
		
		int mode;
		private OperationMode(int mode) {
			this.mode = mode;
		}
		
		public int getMode() {
			return mode;
		}

		public static OperationMode get(int mode) {
			for (OperationMode opMode : values()) {
				if (opMode.getMode()== mode)
					return opMode;
			}
			
			return null;
		}
	}
}