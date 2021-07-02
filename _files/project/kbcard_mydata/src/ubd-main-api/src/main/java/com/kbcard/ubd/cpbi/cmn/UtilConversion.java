package com.kbcard.ubd.cpbi.cmn;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * <pre>
 * 업   무  명 : CXO
 * 서브 업무명 : 공통 데이터변환 유틸리티
 * 설       명 : 
 *     - 암.복호화
 *     - 금액
 *     - 전화번호 등
 * 변 경 이 력
 *  Version  작성자     일  자       내    용
 * ----------------------------------------------------------------------------- 
 *    1.0    주재현     2017.7.12    기존 ConvUtil, FormatUtil 상속
 * </pre>
 ******************************************************************************/
public class UtilConversion
{
	protected static Logger logger = LoggerFactory.getLogger("UtilConversion");
	public static String DEFAULT_CHARSET = "EUC-KR";
	
	/**
	 * Base64 디코딩 처리
	 * @param enc Base64 인코딩 데이터
	 * @return 원문데이터
	 */
	public static String base64Decode(String enc)
	{
		return base64Decode(enc, DEFAULT_CHARSET);
	}
	
	/**
	 * Base64 디코딩 처리
	 * @param enc Base64 인코딩 데이터
	 * @return 원문데이터
	 * @author 2017.11.1 주재현
	 */
	public static String base64Decode(String enc, String charset)
	{
		try
		{
			return new String(base64DecodeByte(enc, charset), charset);
		}
		catch(Exception e)
		{
			logger.error("BASE64 디코딩 중 오류발생!!", e);
		}
		
		return null;
	}
	
	public static byte[] base64DecodeByte(String enc, String charset)
	{
		try
		{
			return Base64.decodeBase64(enc.getBytes(charset));
		}
		catch(Exception e)
		{
			logger.error("BASE64 디코딩 중 오류발생!!", e);
		}
		
		return null;
	}
	
	/**
	 * Base64 디코딩 처리
	 * @param enc Base64 인코딩 데이터
	 * @return 원문데이터
	 */
	public static String base64Decode(byte[] enc)
	{
		return base64Decode(enc, DEFAULT_CHARSET);
	}
	
	/**
	 * Base64 디코딩 처리
	 * @param enc Base64 인코딩 데이터
	 * @return 원문데이터
	 * @author 2017.11.1 주재현
	 */
	public static String base64Decode(byte[] enc, String charset)
	{
		try
		{
			return new String(base64DecodeByte(enc), charset);
		}
		catch(Exception e)
		{
			logger.error("BASE64 디코딩 중 오류발생!!", e);
		}
		
		return null;
	}
	
	public static byte[] base64DecodeByte(byte[] enc)
	{
		try
		{
			return Base64.decodeBase64(enc);
		}
		catch(Exception e)
		{
			logger.error("BASE64 디코딩 중 오류발생!!", e);
		}
		
		return null;
	}
	
	/**
	 *  Base64 인코딩 처리
	 * @param dat 인코딩 대상 평문자열
	 * @return 인코딩데이터
	 */
	public static String base64Encode(byte[] dat)
	{
		return base64Encode(dat, DEFAULT_CHARSET);
	}
	
	/**
	 * Base64 인코딩 처리
	 * @param dat 인코딩 대상 평문자열
	 * @param charset 인코딩
	 * @return 인코딩데이터
	 */
	public static String base64Encode(byte[] dat, String charset)
	{
		try
		{
			return new String(Base64.encodeBase64(dat), charset);
		}
		catch(Exception e)
		{
			//LogMgr.error("BASE64 인코딩 중 오류발생!!", e);
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * 평문문자열을 ZIP 으로 압축 후 base64 인코딩 문자열로 반환한다. 
	 * @param plain 평문문자열
	 * @return ZIP 으로 압축후 base64로 인코딩된 문자열
	 * @author 2019.4.16 / 주재현
	 */
	public static String compress(String plain)
	{
	    return compress(plain, null);
	}
	
	/**
	 * 평문문자열을 ZIP 으로 압축 후 base64 인코딩 문자열로 반환한다. 
	 * @param plain 평문문자열
	 * @param charset 캐릭터셋
	 * @return ZIP 으로 압축후 base64로 인코딩된 문자열
	 * @author 2019.4.16 / 주재현
	 */
	public static String compress(String plain, String charset)
	{
	    if (plain == null)
	        return null;
	    
	    if (charset == null)
	        charset = DEFAULT_CHARSET;
	    
	    String rst = null;
	    
	    try 
	    {
	        byte[] gzip = compress(plain.getBytes(charset)); 
	        rst = base64Encode(gzip, charset);
	        
			// 무조건 압축해서 return
//			if (plain.getBytes().length <= rst.getBytes().length)
//				return null;
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	    
	    return rst;
	}
	
	/**
     * 평문문자열을 ZIP 으로 압축 후 반환한다. 
     * @param plain 평문문자열
     * @return ZIP으로 압축된 바이트
     * @author 2019.4.16 / 주재현
     */
	public static byte[] compress(byte[] plain)
	{
	    Deflater               deflater    = null;
	    ByteArrayOutputStream  out         = null;
	    
	    try
	    {
	    	System.out.println("################################# in compress...");
	        deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(plain);
            deflater.finish();
            
	        out = new ByteArrayOutputStream();
	        byte[] buffer  = new byte[1024];
	        int    compIdx = -1;
	        while (!deflater.finished())
	        {
	            compIdx = deflater.deflate(buffer);
	            out.write(buffer, 0, compIdx);
				System.out.print(".");
	        }
			System.out.println();
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	    finally
	    {
	        if (out        != null) try{ out.close();      }catch(Exception e){}
	        if (deflater   != null) try{ deflater.end();   }catch(Exception e){}
	    }
	    
	    return (out != null) ? out.toByteArray(): null;
	}
	
	/**
	 * ZIP 으로 압축된 바이트를 압축해제 후 반환한다. (base64 변환방식 사용)
	 * @param enc ZIP으로 압축된 바이트 변환문자열
	 * @return 압축해제된 원본 문자열
	 * @author 2019.4.16 / 주재현
	 */
	public static String decompress(String enc)
	{
	    return decompress(enc, null);
	}
	
	/**
	 * ZIP 으로 압축된 바이트를 압축해제 후 반환한다. (base64 변환방식 사용)
	 * @param enc ZIP으로 압축된 바이트 변환문자열
	 * @param charset 캐릭터셋
	 * @return 압축해제된 원본 문자열
	 * @author 2019.4.16 / 주재현
	 */
	public static String decompress(String enc, String charset)
	{
	       if (enc == null)
	            return null;
	        
	        if (charset == null)
	            charset = DEFAULT_CHARSET;
	        
	        try
	        {
	            byte[] gzip    = base64DecodeByte(enc, charset);
	            byte[] plain   = decompress(gzip);
	            return new String(plain, charset);
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	        
	        return null;
	}
	
	/**
	 * ZIP 으로 압축된 바이트를 압축해제 후 반환한다.
	 * @param gzip ZIP으로 압축된 바이트
	 * @return ZIP 압축해제 된 바이트
	 * @author 2019.4.16 / 주재현
	 */
	public static byte[] decompress(byte[] gzip)
    {
	    Inflater               inflater    = null;
        ByteArrayOutputStream  out         = null;
        
        try
        {
        	System.out.println("################################# in decompress...");
            inflater = new Inflater();
            inflater.setInput(gzip);
            
            out = new ByteArrayOutputStream();
            byte[] buffer  = new byte[1024];
            int    compIdx = -1;
            while (!inflater.finished())
            {
                compIdx = inflater.inflate(buffer);
                out.write(buffer, 0, compIdx);
				System.out.print(".");
	        }
			System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (out        != null) try{ out.close();      }catch(Exception e){}
            if (inflater   != null) try{ inflater.end();   }catch(Exception e){}
        }
        
        return (out != null) ? out.toByteArray(): null;
    }
}
