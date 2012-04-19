package yuntian.pi.camera;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import java.util.ArrayList;

import yuntian.pi.entity.ImageEntity;

public class YuntianCameraActivity extends Activity {
	
    private final int CAMERA_PIC_REQUEST=1337;
    private ImageButton imageButton1;
    private Button button1;
    private Button button2;
    private Button button3;
    private ContentResolver myResolver;
    private ArrayList<ImageEntity> mylist=new ArrayList<ImageEntity>();
    private int listIndex=0;
    private SOURCE_TYPE current_src_type;
    
    enum SOURCE_TYPE
    {
    	IMAGE_NONE,
    	CAMERA_IMAGE,
    	THUMBNAIL_IMAGE,
    	ORIGIN_IMAGE,
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window=getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);    
        setContentView(R.layout.main);
        initImageData();
        initApp();
    
    }
    
    private void initApp()
    {

        myResolver=getContentResolver();
        button1=(Button)findViewById(R.id.button1);
        button2=(Button)findViewById(R.id.button2);
        button3=(Button)findViewById(R.id.button3);
        imageButton1 =(ImageButton)findViewById(R.id.imageButton1);
        imageButton1.setAdjustViewBounds(false);
        current_src_type=SOURCE_TYPE.IMAGE_NONE;
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                current_src_type=SOURCE_TYPE.CAMERA_IMAGE;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,CAMERA_PIC_REQUEST);
            }
        });
        
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	showThumbnailImage();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	showOriginImage();
            }
        });
        
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	OnImageButtonClick();
            }
        });
        
    }
    
    private void OnImageButtonClick()
    {
    	if(SOURCE_TYPE.ORIGIN_IMAGE==current_src_type)
    	{
        	showOriginImage();   		
    	}
    	else if(SOURCE_TYPE.THUMBNAIL_IMAGE==current_src_type)
    	{
        	showThumbnailImage(); 		
		}
    }
    
    
    
    private void showThumbnailImage()
    {
        current_src_type=SOURCE_TYPE.THUMBNAIL_IMAGE;
        if(!mylist.isEmpty())
        {
            Bitmap thumbnail=loadThumbnailImage(Uri.withAppendedPath(
                														MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                														Integer.toString(mylist.get(listIndex).getImageID()) ).toString()
            														);
            imageButton1.setImageBitmap(thumbnail);
            imageButton1.setAdjustViewBounds(false);
            
            if(listIndex+1<mylist.size())
            {
                listIndex++;
            }
            else
            {
                listIndex=0;
            }
        }
    	
    }
    
    private void showOriginImage()
    {
        current_src_type=SOURCE_TYPE.ORIGIN_IMAGE;
        if(!mylist.isEmpty())
        {
            Bitmap originMap=BitmapFactory.decodeFile( mylist.get(listIndex).getImagePath(), null ); 

            imageButton1.setImageBitmap(originMap);
            imageButton1.setAdjustViewBounds(true);
            
            if(listIndex+1<mylist.size())
            {
                listIndex++;
            }
            else
            {
                listIndex=0;
            }
        }
    	
    }
    
    private void initImageData()
    {
        //*-----------讀取SD卡上的圖片信息-------------------------------------------------------*
        //設定縮圖的ID列
          String[] projection = { MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA};
          String selection="";
          String [] selectionArgs = null;
          mylist.clear();
          Cursor  mImageCursor = managedQuery( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
        		  					projection, selection, selectionArgs, null );  
          if ( mImageCursor != null ) 
          {         
        	  mImageCursor.moveToFirst();    
              for(int i=0;i<mImageCursor.getCount();i++)
              { 
            	  ImageEntity entity=new ImageEntity();
               	  int imageID = mImageCursor.getInt( mImageCursor.getColumnIndex(MediaStore.Images.Media._ID) ); 
               	  String imagePath=mImageCursor.getString( mImageCursor.getColumnIndex(MediaStore.Images.Media.DATA) ); 
               	  entity.setImageID(imageID);
               	  entity.setImagePath(imagePath);
            	  mylist.add(entity);
            	  mImageCursor.moveToNext();
              } 
              listIndex=0;
          } 
          else 
          {         
        	  //Log.i(TAG, "System media store is empty.");     
          } 

    }
      
    public Bitmap loadFullImage( Context context, Uri photoUri  ) 
    {     
    	Cursor photoCursor = null;      
	    try 
	    {         
		    	// Attempt to fetch asset filename for image         
		    	String[] projection = { MediaStore.Images.Media.DATA };        
		    	photoCursor = context.getContentResolver().query( photoUri,projection, null, null, null );         
				if ( photoCursor != null && photoCursor.getCount() == 1 ) 
				{             
					photoCursor.moveToFirst();             
					String photoFilePath = photoCursor.getString(                 
					photoCursor.getColumnIndex(MediaStore.Images.Media.DATA) );              
					// Load image from path             
					return BitmapFactory.decodeFile( photoFilePath, null );         
				}     
	    } 
	    finally 
	    {         
	    	if ( photoCursor != null )
	    	{             
	    		photoCursor.close();         
	    		
	    	}     
	    }      
	    return null;
	} 

    protected Bitmap loadThumbnailImage( String url ) 
    {     // Get original image ID     
    	int originalImageId = Integer.parseInt(url.substring(url.lastIndexOf("/") + 1, url.length()));      
    	// Get (or create upon demand) the micro thumbnail for the original image.     
    	return MediaStore.Images.Thumbnails.getThumbnail(
    				getContentResolver(),                         
    				originalImageId, 
    				MediaStore.Images.Thumbnails.MICRO_KIND, 
    				null); 
    }
    
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==CAMERA_PIC_REQUEST)
        {
            try
            {
                Object  obj=data.getExtras().get("data");
                if(null!=obj)
                {
                    @SuppressWarnings("unused")
                    Bitmap thumbnail=(Bitmap)obj;
                    imageButton1.setImageBitmap(thumbnail);
                    imageButton1.setAdjustViewBounds(true);
                }
            }
            catch(Exception e)
            {
                   
            }
        }        
    }  
    

}

