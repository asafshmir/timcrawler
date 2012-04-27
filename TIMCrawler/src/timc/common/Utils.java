package timc.common;

public class Utils 
{
	public enum OperationMode
	{
		Normal(0),
		NeverSeed(1),
		HalfSeedDropNewPieces(2);
		
		int m_mode;
		private OperationMode(int mode)
		{
			m_mode = mode;
		}
		public int getMode()
		{
			return m_mode;
		}

		public static OperationMode get(int mode)
		{
			for (OperationMode opMode : values())
			{
				if (opMode.getMode()== mode)
					return opMode;
			}
			
			return null;
		}
	}
}
