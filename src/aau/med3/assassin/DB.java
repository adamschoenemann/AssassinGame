package aau.med3.assassin;

import org.json.JSONObject;

public class DB {
	
	public static final class users {
		
		public static final String ID = "ID",
							email = "email",
							password = "password",
							phone_ID = "phone_ID",
							alive = "alive",
							target_ID = "target_ID",
							points = "points",
							education = "education";
		
	}
	public static final class phones {
		public static final String ID = "ID", MAC = "MAC";
	}
	
	public static JSONObject userCols;
}
