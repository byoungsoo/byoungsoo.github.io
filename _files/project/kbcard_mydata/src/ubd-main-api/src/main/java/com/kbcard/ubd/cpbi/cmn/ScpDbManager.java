package com.kbcard.ubd.cpbi.cmn;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.penta.scpdb.ScpDbAgent;

public class ScpDbManager {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private static ScpDbManager instance;
	private ScpDbAgent agent = null;
	private int PLAIN_MAX_SIZE = 1024;
	
//	private String iniFile = null; 
//	private String charSet = null; 
//	private String serviceKey = null; 
	
	private ScpDbManager()
	{
		try
		{
			System.out.println("###### start create ScpDbAgent instance");
			agent 	= new ScpDbAgent();
			System.out.println("###### created ScpDbAgent instance");
			//agent.AgentInit( KBCardConstantUtil.SCP_INI_FILE );
			System.out.println("###### initialize ScpDbAgent.ini");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized static ScpDbManager getInstance()
	{
		System.out.println("###### in getInstance()");
		try
		{
			System.out.println("###### start create ScpDbManager instance");
			if(instance == null) instance = new ScpDbManager();
			System.out.println("###### created ScpDbManager instance");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return instance;
	}

	/**
	 * 암호화 파일을 평문 파일로 변환 (복호화)
	 * @param svcKey 암호화서비스KEY
	 * @param srcFile 암호화파일 
	 * @param dstFile 복호화파일 
	 * @return 성공 = 1, 실패 = 0
	 */
	public int decryptFile(String iniFile, String serviceKey, String srcFile, String dstFile)
	{
		try
		{
			logger.info("JAVA CLASS PATH : " + System.getProperty("java.class.path"));
			logger.info("JAVA LIBRARY PATH : " + System.getProperty("java.library.path"));

		    agent.ScpDecFile(iniFile, serviceKey, srcFile, dstFile);
			return 1;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.info("복호화 중 오류!!, 요청값=["+new String(srcFile)+"]", e);
			return 0;
		}
	}

	/**
	 * 암호화 텍스트를 평문 텍스트로 변환 (복호화)
	 */
	public String decB64(String iniFile, String serviceKey, String encText, String encoding)
	{
		try
		{
		    return agent.ScpDecB64(iniFile, serviceKey, encText, encoding);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.info("암호화 중 오류!!, 요청값=["+encText+"]", e);
			return null;
		}
	}

	/**
	 * 평문 텍스트를 암호화 텍스트로 변환 (암호화)
	 */
	public String encB64(String iniFile, String serviceKey, String planeText, String encoding)
	{
		try
		{
		    return agent.ScpEncB64(iniFile, serviceKey, planeText, encoding);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.info("복호화 중 오류!!, 요청값=["+planeText+"]", e);
			return null;
		}
	}
	
	/**
	 * 대량데이터 암호화
	 * @param inputData 암호화 대상문자열
	 * @return 암호화 데이터
	 */
	public String encryptSplit(String iniFile, String serviceKey, byte[] inputData)
    {
        StringBuilder sb = null;
        
        try
        {
            /* Encryption */
			logger.debug("################################# in encryptSplit...");
            sb  = new StringBuilder();                          // 암호화 정보가 저장될 변수
            
            int     inputDataLen    = inputData.length;         // 평문 전체 길이
            int     readLen         = 0;                        // 읽은 길이
            int     remainLen       = inputDataLen;             // 평문 남은 길이 ( 평문 전체 길이 - 읽은 길이 )

            byte[] buffer = null;
            while ( readLen < inputDataLen )
            {
                // plain_max_size 만큼 읽는다.
                if ( remainLen <= PLAIN_MAX_SIZE)
                {
                    buffer = new byte[remainLen];
                    System.arraycopy(inputData, readLen, buffer, 0, remainLen);
                }
                else
                {
                    if (buffer == null)
                        buffer = new byte[PLAIN_MAX_SIZE];
                    
                    System.arraycopy(inputData, readLen, buffer, 0, PLAIN_MAX_SIZE);
                }

                sb.append(UtilConversion.base64Encode(agent.ScpEncB64(iniFile, serviceKey, buffer)));

                // 암호화 값을 구분하기 위해 | 를 추가한다. B64에는 | 값이 포함되지 않는다.
                sb.append("|");
                readLen     += PLAIN_MAX_SIZE;
                remainLen   -= PLAIN_MAX_SIZE;
				System.out.print(".");
	        }
			System.out.println();
            return sb.toString();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        return null;
    }
	

	/**
	 * 차세대 대량데이터 복호화
	 * @param encData 대량데이터 암호화문자열
	 * @return 복호화된 데이터
	 */
	public byte[] decryptSplit(String iniFile, String serviceKey, String encData)
    {
	    ByteBuffer buffer = null;

        try
        {
			logger.debug("################################# in decryptSplit...");
            buffer = ByteBuffer.allocate(encData.length());
            buffer.clear();
            
            String[] encList = encData.split("\\|");
            for(String enc: encList)
            {
                byte[] dbenc = UtilConversion.base64DecodeByte(enc.getBytes());
                buffer.put(agent.ScpDecB64(iniFile, serviceKey, dbenc));
				System.out.print(".");
	        }
			System.out.println();
            
            buffer.flip();
           
//			if (new String(UtilConversion.decompress(buffer.array())).length() < 1) {
//				return decryptSplitAsIs(encData);
//			}

            return buffer.array();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        return null;
    }	
		
	
}
