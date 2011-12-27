/**
 * The ImageManager plugin javascript.
 * @author $Author: Wei Zhuo $
 * @version $Id: image-manager.js 26 2004-03-31 02:35:21Z Wei Zhuo $
 * @package ImageManager
 */

/**
 * To Enable the plug-in add the following line before HTMLArea is initialised.
 *
 * HTMLArea.loadPlugin("ImageManager");
 *
 * Then configure the config.inc.php file, that is all.
 * For up-to-date documentation, please visit http://www.zhuo.org/htmlarea/
 */

/**
 * It is pretty simple, this file over rides the HTMLArea.prototype._insertImage
 * function with our own, only difference is the popupDialog url
 * point that to the php script.
 */

function ImageManager(editor)
{

}

ImageManager._pluginInfo = {
	name          : "ImageManager",
	version       : "1.0",
	developer     : "Xiang Wei Zhuo",
	developer_url : "http://www.zhuo.org/htmlarea/",
	license       : "htmlArea"
};

// default Xinha layout. plugins are beneath the Xinha directory.
// Note the trailing &. Makes forming our URL's easier. 
//
// To change the backend, just set this config variable in the calling page.
// The images_url config option is used to strip out the directory info when
// images are selected from the document.

HTMLArea.Config.prototype.ImageManager =
{
  'backend'    : _editor_url + 'plugins/ImageManager/backend.php?__plugin=ImageManager&',

  //  It's useful to pass the configuration to the backend through javascript
  //  (this saves editing the backend config itself), but the problem is
  //  how do you make it so that the enduser can not sneakily send thier own
  //  config to the server (including directory locations etc!).
  //
  //  Well, we specify 3 config variables (if the first is given all 3 are required)
  //  first in backend_config we provide the backend configuration (in the format
  //  required by the backend, in the case of PHP this is a serialized structure).  We do not
  //  need to provide a complete configuration here, it will be merged with defaults.
  //
  //  Then in backend_config_secret_key_location we store the name of a key in a
  //  session structure which stores a secret key (anything random), for example
  //  when making the Xinha editor in PHP we might do
  //  <?php $_SESSION['Xinha:ImageManager'] = uniqid('secret_'); ?>
  //  xinha_config.ImageManager.backend_config_secret_key_location = 'Xinha:ImageManager';
  //
  //  Then finally in backend_config_hash we store an SHA1 hash of the config combined
  //  with the secret.
  //
  //  A full example in PHP might look like
  //
  //  <?php
  //   $myConfig = array('base_dir' = '/home/your/directory', 'base_url' => '/directory')
  //   $myConfig = serialize($myConfig);
  //   if(!isset($_SESSION['Xinha:ImageManager'])) $_SESSION['Xinha:ImageManager'] = uniqid('secret_');
  //   $secret = $_SESSION['Xinha:ImageManager'];
  //  ?>
  //  xinha_config.ImageManager.backend_config      = '<?php echo jsaddslashes($myConfig)?>';
  //  xinha_config.ImageManager.backend_config_hash = '<?php echo sha1($myConfig . $secret)?>';
  //  xinha_config.ImageManager.backend_config_secret_key_location = 'Xinha:ImageManager';
  //
  // (for jsspecialchars() see http://nz.php.net/manual/en/function.addcslashes.php)
  //
  //
  'backend_config'     : null,
  'backend_config_hash': null,
  'backend_config_secret_key_location': 'Xinha:ImageManager'
};

// Over ride the _insertImage function in htmlarea.js.
// Open up the ImageManger script instead.

HTMLArea.prototype._insertImage = function(image) {

	var editor = this;	// for nested functions
	var outparam = null;
	if (typeof image == "undefined") {
		image = this.getParentElement();
		if (image && !/^img$/i.test(image.tagName))
			image = null;
	}

	// the selection will have the absolute url to the image. 
	// coerce it to be relative to the images directory.
	//
	// FIXME: we have the correct URL, but how to get it to select?
	// FIXME: need to do the same for MSIE.

	if ( image )
		{

		outparam =
			{
			f_url    : HTMLArea.is_ie ? image.src : image.src,
			f_alt    : image.alt,
			f_border : image.style.borderWidth ? image.style.borderWidth : image.border,
			f_align  : image.align,
			f_padding: image.style.padding,
			f_margin : image.style.margin,
			f_width  : image.width,
			f_height  : image.height,
      f_backgroundColor: image.style.backgroundColor,
      f_borderColor: image.style.borderColor
			};

    function shortSize(cssSize)
    {
      if(/ /.test(cssSize))
      {
        var sizes = cssSize.split(' ');
        var useFirstSize = true;
        for(var i = 1; i < sizes.length; i++)
        {
          if(sizes[0] != sizes[i])
          {
            useFirstSize = false;
            break;
          }
        }
        if(useFirstSize) cssSize = sizes[0];
      }
      return cssSize;
    }
    outparam.f_border = shortSize(outparam.f_border);
    outparam.f_padding = shortSize(outparam.f_padding);
    outparam.f_margin = shortSize(outparam.f_margin);

		} // end of if we selected an image before raising the dialog.

	// the "manager" var is legacy code. Should probably reference the
	// actual config variable in each place .. for now this is good enough.

	// alert( "backend is '" + editor.config.ImageManager.backend + "'" );

	var manager = editor.config.ImageManager.backend + '__function=manager';
  if(editor.config.ImageManager.backend_config != null)
  {
    manager += '&backend_config='
      + encodeURIComponent(editor.config.ImageManager.backend_config);
    manager += '&backend_config_hash='
      + encodeURIComponent(editor.config.ImageManager.backend_config_hash);
    manager += '&backend_config_secret_key_location='
      + encodeURIComponent(editor.config.ImageManager.backend_config_secret_key_location);
  }

	Dialog(manager, function(param) {
		if (!param) {	// user must have pressed Cancel
			return false;
		}
		var img = image;
		if (!img) {
			if (HTMLArea.is_ie) {
        var sel = editor._getSelection();
        var range = editor._createRange(sel);
        editor._doc.execCommand("insertimage", false, param.f_url);
				img = range.parentElement();
				// wonder if this works...
				if (img.tagName.toLowerCase() != "img") {
					img = img.previousSibling;
				}
			} else {
				img = document.createElement('img');
        img.src = param.f_url;
        editor.insertNodeAtSelection(img);
			}
		} else {			
			img.src = param.f_url;
		}
		
		for (field in param) {
			var value = param[field];
			switch (field) {
			    case "f_alt"    : img.alt	 = value; break;
			    case "f_border" :
          if(value.length)
          {           
            img.style.borderWidth = /[^0-9]/.test(value) ? value :  (parseInt(value) + 'px');
            if(img.style.borderWidth && !img.style.borderStyle)
            {
              img.style.borderStyle = 'solid';
            }
          }
          else
          {
            img.style.borderWidth = '';
            img.style.borderStyle = '';
          }
          break;
          
          case "f_borderColor": img.style.borderColor = value; break;
          case "f_backgroundColor": img.style.backgroundColor = value; break;
            
          case "f_padding": 
          {
            if(value.length)
            {
              img.style.padding = /[^0-9]/.test(value) ? value :  (parseInt(value) + 'px'); 
            }
            else
            {
              img.style.padding = '';
            }
          }
          break;
          
          case "f_margin": 
          {
            if(value.length)
            {
              img.style.margin = /[^0-9]/.test(value) ? value :  (parseInt(value) + 'px'); 
            }
            else
            {
              img.style.margin = '';
            }
          }
          break;
          
			    case "f_align"  : img.align	 = value; break;
            
          case "f_width" : 
          {
            if(!isNaN(parseInt(value))) { img.width  = parseInt(value); } else { img.width = ''; }
          }
          break;
          
				  case "f_height":
          {
            if(!isNaN(parseInt(value))) { img.height = parseInt(value); } else { img.height = ''; }
          }
          break;
			}

		}
		
		
	}, outparam);
};
