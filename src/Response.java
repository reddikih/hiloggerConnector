
public class Response {
	private final byte[] rawData;
	
	public Response(byte[] rawData) {
		this.rawData = rawData;
	}
	
	// MemoryHiLoggerの状態を取得
	public byte getState() {
		if(rawData[0] == 0x02 && rawData[1] == 0x01) {
			switch(rawData[2]) {
			case 0x50:	// スタートコマンド
			case 0x51:	// ストップコマンド
			case 0x57:	// 測定状態要求コマンド
			case 0x58:	// アプリシステムトリガコマンド
				return rawData[5];
			}
		}
		return (byte) 0xff;	// 不明なコマンド
	}
	
	// メモリ内データ数を取得
	public long getNumOfData() {
		String raw = "";
		if(rawData[0] == 0x02 && rawData[1] == 0x01) {
			switch(rawData[2]) {
			case 0x55:
				for(int i = 13; i < 21; i++) {
					raw += String.format("%02x", rawData[i]);
				}
				return Long.parseLong(raw, 16);
			}
		}
		return -1;
	}
}
