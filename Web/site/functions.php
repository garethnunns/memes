<?php
	session_start();

	// globals
	require_once dirname(__FILE__).'/secure.php'; // database connection

	$sitename = 'Memestagram'; // allow site to be easily rebranded

	$res = "http://memes-store.garethnunns.com/"; // resource server
	$web = "http://memes.garethnunns.com/"; // web server


	// can be added to later, but for now just JPGs and PNGs
	$types = array("image/jpg","image/jpeg","image/png");
	$exts = array("jpg","jpeg","png");

	// image sizes
	$fulls = [1000,2000];
	$squares = [150,400];

	// uploads location
	$ds = DIRECTORY_SEPARATOR;
	$target = dirname( __FILE__ ) . $ds . ".." . $ds . "uploads" . $ds;

	// aws deets
	$aws = array(
		'region'  => 'eu-west-2',
		'version' => '2006-03-01',
		'credentials' => array( // these are stored in the secure file because the ini method failed
			'key'    => AWS_KEY,
			'secret' => AWS_SECRET,
		)
	);
	$bucket = 'memes-store';

	$defaultPics = array( // for now these are hard coded - could list the objects in the bucket
		'profile/default/pepe.png',
		'profile/default/doge.png',
		'profile/default/lol.png'
	);

	function valid($field, $text) {
		// verify the text is valid to be inserted
		// for any $text it will check that $field in the database can take length string
		// or check it's not too few characters
		// returns true or an error string

		// TODO validate name so that it can old have letters and spaces in - \p{L} internationalisation...

		global $dbh;

		list($table, $column) = explode('.',$field);

		$validation = array( // friendly name (null if the same), min (if different), max (if different)
			'user.username' => array(null, 3, 20),
			'user.password' => array(null, 8, 50),
			'user.email' => array(null, null, null),
			'user.firstName' => array('first name', null, null),
			'user.surname' => array(null, null, null)
		);

		try {
			$sql = "SELECT character_maximum_length as len, IS_NULLABLE as n   
					FROM information_schema.columns  
					WHERE table_name = ?
					AND column_name = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array($table,$column));

			$attr = $sth->fetch(PDO::FETCH_OBJ);

			if(isset($validation[$field])) {
				$friendly = is_null($validation[$field][0]) ? $column : $validation[$field][0];
				$min = is_null($validation[$field][1]) ? 0 : $validation[$field][1];
				$max = is_null($validation[$field][2]) ? $attr->len : $validation[$field][2];
			}
			else { // it hasn't been set up in the validation array
				if(!$sth->rowCount()) // couldn't find the field in the database schema
					return "There has been an internal error"; // just to be safe
				
				$friendly = $column;
				$min = 0;
				$max = $attr->len;
			}

			// goldilocks section
			if(strlen($text) > $max) // checks it's not too long
				return "The $friendly must be less than $max characters.";

			if(empty(ltrim($text)) && ($attr->n == "NO")) // check for whether a field can be null
				return "The $friendly can't be left blank.";

			if(strlen($text) < $min) // check it is at least the minimum length
				return "The $friendly must be more than $min characters.";

			if(($field == 'user.username') && ($uError = validUsername($text))!==true) // check the username
				return $uError;

			if(($field == 'user.password') && ($pError = validPassword($text))!==true) // check the password
				return $pError;

			if(($field == 'user.email') && ($eError = validEmail($text))!==true) // check the password
				return $eError;

			return true;
		}
		catch (PDOException $e) {
			echo $e->getMessage();
		}
	}

	function validUsername($username) {
		// many checks are done on the username...

		global $dbh;

		/* 
		there are some names we obviously don't want them to take... (currently nothing protects against offensive usernames)
		these are lifted from this repo: https://github.com/marteinn/The-Big-Username-Blacklist
		They are distributed under the MIT License (https://opensource.org/licenses/MIT), which seems a bit overkill for an array...
		THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
		*/

		$protected = ['400', '401', '403', '404', '405', '406', '407', '408', '409', '410', '411', '412', '413', '414', '415', '416', '417', '421', '422', '423', '424', '426', '428', '429', '431', '500', '501', '502', '503', '504', '505', '506', '507', '508', '509', '510', '511', 'about', 'about-us', 'abuse', 'access', 'account', 'accounts', 'add', 'admin', 'administration', 'administrator', 'advertise', 'advertising', 'aes128-ctr', 'aes128-gcm', 'aes192-ctr', 'aes256-ctr', 'aes256-gcm', 'affiliate', 'affiliates', 'ajax', 'alert', 'alerts', 'alpha', 'amp', 'analytics', 'api', 'app', 'apps', 'asc', 'assets', 'atom', 'auth', 'authentication', 'authorize', 'autoconfig', 'avatar', 'backup', 'banner', 'banners', 'beta', 'billing', 'billings', 'blog', 'blogs', 'board', 'bookmark', 'bookmarks', 'broadcasthost', 'business', 'buy', 'cache', 'calendar', 'campaign', 'captcha', 'careers', 'cart', 'cas', 'categories', 'category', 'cdn', 'cgi', 'cgi-bin', 'chacha20-poly1305', 'change', 'channel', 'channels', 'chat', 'checkout', 'clear', 'client', 'close', 'comment', 'comments', 'community', 'compare', 'compose', 'config', 'connect', 'contact', 'contest', 'cookies', 'copy', 'copyright', 'count', 'create', 'css', 'curve25519-sha256', 'customize', 'dashboard', 'db', 'deals', 'debug', 'delete', 'desc', 'dev', 'developer', 'developers', 'diffie-hellman-group-exchange-sha256', 'diffie-hellman-group14-sha1', 'disconnect', 'discuss', 'dns', 'dns0', 'dns1', 'dns2', 'dns3', 'dns4', 'docs', 'documentation', 'domain', 'download', 'downloads', 'downvote', 'draft', 'drop', 'ecdh-sha2-nistp256', 'ecdh-sha2-nistp384', 'ecdh-sha2-nistp521', 'edit', 'editor', 'email', 'enterprise', 'error', 'errors', 'event', 'events', 'example', 'exception', 'exit', 'explore', 'export', 'extensions', 'false', 'family', 'faq', 'faqs', 'features', 'feed', 'feedback', 'feeds', 'feeds', 'file', 'files', 'filter', 'follow', 'follower', 'followers', 'following', 'fonts', 'forgot', 'forgot-password', 'forgotpassword', 'form', 'forms', 'forum', 'forums', 'friend', 'friends', 'ftp', 'get', 'go', 'group', 'groups', 'guest', 'guidelines', 'guides', 'head', 'header', 'help', 'hide', 'hmac-sha', 'hmac-sha1', 'hmac-sha1-etm', 'hmac-sha2-256', 'hmac-sha2-256-etm', 'hmac-sha2-512', 'hmac-sha2-512-etm', 'home', 'host', 'hosting', 'hostmaster', 'htpasswd', 'http', 'httpd', 'https', 'icons', 'images', 'imap', 'img', 'import', 'info', 'insert', 'investors', 'invitations', 'invite', 'invite', 'invites', 'invoice', 'is', 'isatap', 'issues', 'it', 'jobs', 'join', 'js', 'json', 'learn', 'legal', 'licensing', 'limit', 'load', 'local', 'localdomain', 'localhost', 'lock', 'login', 'logout', 'lost-password', 'mail', 'mail0', 'mail1', 'mail2', 'mail3', 'mail4', 'mail5', 'mail6', 'mail7', 'mail8', 'mail9', 'mailer-daemon', 'mailerdaemon', 'map', 'marketing', 'marketplace', 'master', 'me', 'media', 'member', 'members', 'message', 'messages', 'mis', 'mobile', 'moderator', 'modify', 'more', 'mx', 'my', 'network', 'new', 'news', 'newsletter', 'newsletters', 'next', 'nil', 'no-reply', 'nobody', 'noc', 'none', 'noreply', 'notification', 'notifications', 'ns', 'ns0', 'ns1', 'ns2', 'ns3', 'ns4', 'ns5', 'ns6', 'ns7', 'ns8', 'ns9', 'null', 'oauth', 'oauth2', 'offer', 'offers', 'online', 'openid', 'order', 'orders', 'overview', 'owner', 'page', 'pages', 'partners', 'passwd', 'password', 'pay', 'payment', 'payments', 'photo', 'photos', 'plans', 'plugins', 'policies', 'policy', 'pop', 'pop3', 'popular', 'portfolio', 'post', 'postfix', 'postmaster', 'poweruser', 'preferences', 'premium', 'press', 'previous', 'pricing', 'print', 'privacy', 'privacy-policy', 'private', 'product', 'profile', 'profiles', 'project', 'projects', 'public', 'purchase', 'put', 'quota', 'redirect', 'reduce', 'refund', 'refunds', 'register', 'registration', 'remove', 'replies', 'reply', 'report', 'request', 'request-password', 'reset', 'reset-password', 'response', 'return', 'returns', 'review', 'reviews', 'root', 'rootuser', 'rsa-sha2-2', 'rsa-sha2-512', 'rss', 'rules', 'sales', 'save', 'script', 'sdk', 'search', 'security', 'select', 'services', 'session', 'sessions', 'settings', 'setup', 'share', 'shift', 'shop', 'signin', 'signup', 'site', 'sitemap', 'sites', 'smtp', 'sort', 'source', 'sql', 'ssh', 'ssh-rsa', 'ssl', 'ssladmin', 'ssladministrator', 'sslwebmaster', 'stage', 'staging', 'stat', 'static', 'statistics', 'stats', 'status', 'store', 'stylesheet', 'stylesheets', 'subdomain', 'subscribe', 'sudo', 'super', 'superuser', 'support', 'survey', 'sync', 'sysadmin', 'system', 'tablet', 'tag', 'tags', 'team', 'telnet', 'terms', 'terms-of-use', 'test', 'testimonials', 'theme', 'themes', 'today', 'tools', 'topic', 'topics', 'tour', 'training', 'translate', 'translations', 'trending', 'trial', 'true', 'umac-128', 'umac-128-etm', 'umac-64', 'umac-64-etm', 'undefined', 'unfollow', 'unsubscribe', 'update', 'upgrade', 'usenet', 'user', 'username', 'users', 'uucp', 'var', 'verify', 'video', 'view', 'void', 'vote', 'webmail', 'webmaster', 'website', 'widget', 'widgets', 'wiki', 'wpad', 'write', 'www', 'www-data', 'www1', 'www2', 'www3', 'www4', 'you', 'yourname', 'yourusername', 'zlib'];

		if(in_array($username, $protected))
			$ret .= " That username '".htmlspecialchars($username)."' cannot be taken.";

		// check the username is valid in terms of what it contains
		preg_match('/^([_|-]?[[:alnum:]][_|-]?){0,20}/', $username, $usermatches, PREG_OFFSET_CAPTURE);
		if(($usermatches[0][0]!=$username) || ($usermatches[0][1]!=0))
			$ret .= " The username can only contain alphanumeric characters (letters and numbers), as well as underscores and dashes (_ or -). <i>You can't have two underscores or dashes next to each other</i>.";


		// check the username hasn't been taken by another user
		try {
			$sql = "SELECT iduser FROM user WHERE LOWER(username) = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array(strtolower($username)));

			if($sth->fetchColumn()>0) 
				$ret .= " The username '".htmlspecialchars($username)."' has already been taken.";
		}
		catch (PDOException $e) {
			echo $e->getMessage();
		}

		return isset($ret) ? $ret : true;
	}

	function validPassword($password) {
		// check the $password is strong enough
		// must have a lowercase, uppercase and number in
		preg_match('/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]{8,50}$/', $password, $passmatches, PREG_OFFSET_CAPTURE);
		if(($passmatches[0][0]!=$password) || ($passmatches[0][1]!=0))
			return " The password must contain a lowercase letter, an uppercase letter and a number.";

		return true;
	}

	function validEmail($email) {
		// checks it's a valid email and it hasn't already been used

		global $dbh;

		if(!filter_var($email, FILTER_VALIDATE_EMAIL)) // built in function to check emails
			return " This email address doesn't look right.";

		// check the email hasn't already used by another user
		try {
			$sql = "SELECT COUNT(iduser) FROM user WHERE lower(email) = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array(strtolower($email)));

			if($sth->fetchColumn()>0) 
				return " The email address '".htmlspecialchars($email)."' has already been used.";
		}
		catch (PDOException $e) {
			echo $e->getMessage();
		}

		return true;
	}

	function validImage($file) {
		// check's that the $file is an image and the right size
		// expects an array in the standard PHP file style
		// returns TRUE or an array of errors if not

		global $types, $exts;

		$errors = array();

		if(!isset($file['name'])) array_push($errors, "A file wasn't uploaded");
		else { // a file was uploaded
			if($file['error'] !== UPLOAD_ERR_OK) array_push($errors, "Upload failed with error code " . $file['error']);
			else { // there were no errors uploading the file
				if(!in_array(strtolower(pathinfo($file["name"],PATHINFO_EXTENSION)), $exts)) { // check the extension
					$error = "That file extension isn't allowed, the accepted types are ";
					foreach ($exts as $key => $ext)
						$error .= ($key < count($exts)-1) ? (($key != count($exts)-2) ? ".$ext, " : ".$ext ")   : "& .$ext";
					array_push($errors, $error);
				}
				else { // image at least has the right file extension
					$info = getimagesize($file["tmp_name"]);
					if($info === FALSE) array_push($errors, "Unable to determine the type of image that was uploaded");
					else { // probably is an image

						if(!in_array($info["mime"], $types)) { // not one of the allowed mime types
							$error = "That type of image is allowed, the accepted types are ";
							foreach ($types as $key => $type) {
								$type = substr($type, 6);
								$error .= ($key < count($exts)-1) ? (($key != count($exts)-2) ? "$type, " : "$type ")   : "& $type";
							}
							array_push($errors, $error);
						}
						else {
							// we now know we have a JPG or PNG - yay
							// we don't let them past this point if it's not a JPG or PNG, from now on we might give them a few more errors

							// check the file size
							if($file['size'] > 10 * 1000 * 1000) array_push($errors, "The maximum file size is 10MiB");

							// this section is to do with the dimensions
							list($w,$h) = $info;

							// first check is to make sure it's high enough resolution
							// for now I think anything less that 300x300 is too small
							if(($w < 400) || ($h < 400)) array_push($errors, "The image is too low resolution (minimum of 400x400)");

							// next check if it's too big
							// this will save the server having to resize massive images
							if($w * $h > 12000000) array_push($errors, "The image is too large (maximum resolution of 12MP)");

							// now we'll check the aspect ratio
							$ratio = $w/$h;

							// portrait images
							if($ratio < (4/5)) array_push($errors, "The image is too tall (mimimum ratio of 4:5)");

							// landscape images
							if($ratio > (16/9)) array_push($errors, "The image is too wide (mimimum ratio of 16:9)");
						}
					}
				}
			}
		}

		return empty($errors) ? true : $errors;
	}

	function resizeImage($file, &$created) {
		// validates & resizes & crops the $file and stores the locations of these in the $created array
		// expects an array in the standard PHP file style
		// returns TRUE or an array of errors if not

		// make sure it is a valid image
		if(($errors = validImage($file)) !== true) // there were errors validating the image
			return $errors;

		global $target, $fulls, $squares;

		ini_set('memory_limit', '-1');

		$errors = array();

		$info = getimagesize($file["tmp_name"]);

		if($info["mime"] == "image/jpg" || $info["mime"] == "image/jpeg") $type = "jpg";
		elseif ($info["mime"] == "image/png") $type = "png";
		else array_push($errors, "Unrecognised image type");

		// load the image
		$error = "Couldn't load the image";
		if($type == "jpg") if(!$src = imagecreatefromjpeg($file["tmp_name"])) array_push($errors, $error);
		if($type == "png") if(!$src = imagecreatefrompng($file["tmp_name"])) array_push($errors, $error);

		// get the current width and height
		list($w,$h) = $info;

		$ratio = $w/$h;

		$created = array();

		foreach ($fulls as $size) { // these will all be resized, with a width of $size
			$newH = $size/$ratio;
			$resized = imagecreatetruecolor($size, $newH);
			imagecopyresampled($resized, $src, 0, 0, 0, 0, $size, $newH, $w, $h);
			
			$fileName = tempnam($target, $size); // get a unique name
			unlink($fileName); // delete the temporary file - we don't really need it, we just wanted the name
			$fileName .= $type == "jpg" ? '.jpg' : ($type == "png" ? '.png' : '');
			
			$error = "There was an error saving the ".$size."px version of the image";

			if($type == "jpg") if(!imagejpeg($resized,$fileName)) array_push($errors, $error);
			if($type == "png") if(!imagepng($resized,$fileName)) array_push($errors, $error);
			
			$created['full'][$size] = $fileName;
		}

		foreach ($squares as $size) { // these will all be resized & cropped, with a width & height of $size
			// defaults for a square image
			$width = $w;
			$height = $h;
			$x = 0;
			$y = 0;

			if($width > $height) { // landscape
				$x = floor(($width/2) - ($height/2)); // middle - half of the square
				$width = $height;
			}
			elseif($height > $width) { // portrait
				$y = floor(($height/2) - ($width/2)); // middle - half of the square
				$height = $width;
			}

			$resized = imagecreatetruecolor($size, $size);
			imagecopyresampled($resized, $src, 0, 0, $x, $y, $size, $size, $width, $height);
			
			$fileName = tempnam($target, $size); // get a unique name
			unlink($fileName); // delete the temporary file - we don't really need it, we just wanted the name
			$fileName .= $type == "jpg" ? '.jpg' : ($type == "png" ? '.png' : '');
			
			$error = "There was an error saving the ".$size."px version of the image";

			if($type == "jpg") if(!imagejpeg($resized,$fileName)) array_push($errors, $error);
			if($type == "png") if(!imagepng($resized,$fileName)) array_push($errors, $error);
			
			$created['thumb'][$size] = $fileName;
		}

		imagedestroy($src);

		return empty($errors) ? true : $errors;
	}

	function storeMeme($key, $images, $caption, $lat=null, $long=null, &$link=null) {
		// store the meme in the database and transfer the images to Amazon S3
		// expected variables
		// $key 		the user's key in the database
		// $images		an array of images in the style created by resizeImage
		// caption		the caption to go with the image
		// Will return TRUE on success, a string as an error if not

		global $dbh; // database connection

		global $aws, $bucket; // aws details

		global $web; // get the web server location

		// check the caption is valid - you may want to do this before resizing the images
		if(($cerror = valid('meme.caption',$_POST['caption'])) !== true) 
			$error = $cerror;
		else {
			// check the s3 connection first
			$s3 = Aws\S3\S3Client::factory($aws);
			if(!class_exists('Aws\S3\S3Client') || !class_exists('Aws\CommandPool'))
				$error = 'There was an error forming a connection to the resource server';
			else {
				if(!$s3->doesBucketExist($bucket)) // check the bucket is there
					$error = 'There was an error locating the resource server';
				else {
					try {
						if(($user = userDetails($key)) === false) $error = "There was an error with the key";
						else {
							// store in db
							$sizes = array();

							foreach ($images as $type => $csizes)
								foreach ($csizes as $size => $image) {
									if(!isset($sizes[$type])) $sizes[$type] = array();
									array_push($sizes[$type], $size);
									$ext = strtolower(pathinfo($image,PATHINFO_EXTENSION));
								}

							$sth = $dbh->prepare("INSERT INTO meme (iduser, sizes, ext, caption, latitude, longitude) 
								VALUES (?, ?, ?, ?, ?, ?)");

							$sth->execute(array(
								$user->iduser,
								json_encode($sizes),
								$ext,
								$caption,
								$lat,
								$long
							));

							$id = $dbh->lastInsertId();
						}
					}
					catch (PDOException $e) {
						$error = "There was an error adding the meme to the database";
					}

					if(!isset($error)) {
						// copy the images over to aws
						$commands = array();

						foreach ($images as $type => $csizes) {
							foreach ($csizes as $size => $image) {
								$commands[] = $s3->getCommand('PutObject', array(
									'Bucket'     => $bucket,
									'Key'        => $type.'/'.$size.'/'.$id.'.'.$ext,
									'SourceFile' => $image,
									'Metadata'   => array(
										'User' => $user->iduser
									)
								));
							}
						}

						$pool = new Aws\CommandPool($s3, $commands);

						// Initiate the pool transfers
						$promise = $pool->promise();

						try { // Force the pool to complete synchronously
							$result = $promise->wait();
						}
						catch (AwsException $e) {
							$error = "There was an error transferring the images to the resource server";
						}

						if($link!==null) $link = $web . '/'. $user->username . '/' . $id;
					}
				}
			}
		}

		// delete all of the images off the server (this happens even if errors were thrown)
		foreach ($images as $type => $csizes)
			foreach ($csizes as $size => $image)
				unlink($image);

		return isset($error) ? $error : true;
	}

	function userDetails($key) {
		// returns an object containing all of the details
		// expects the user's key as an input

		global $dbh; // database connection

		try {
			$sql = "SELECT user.*
					FROM user
					WHERE ukey = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array($key)); // sanitise user input

			if($sth->rowCount()==0) return false;

			return $sth->fetch(PDO::FETCH_OBJ);;
		}
		catch (PDOException $e) {
			return false;
		}
	}

	function memeFeed($key,$start=0,$thumb=400,$full=1000) {
		// the user's meme feed
		// expected the user's $key
		// returns an array with the first 20 memes in their feed, starting at $start

		/* TODO
			* repostable
			* liked already
			* remove $e in errors when out of dev
		*/

		global $dbh; // database connection

		global $web, $res; // servers

		$memes = array();

		if(($user = userDetails($key)) === false){
			$memes['error'] = "Invalid user key";
			goto error;
		}


		try {
			$sql = "
SELECT m.*, p.username AS pUsername, CONCAT(p.firstName, ' ', p.surname) as pName, p.picUri as pPicUri,
o.iduser AS oIduser, o.username AS oUsername, CONCAT(o.firstName, ' ', o.surname) as oName, o.picUri as oPicUri,
(
	SELECT COUNT(s.idmeme)
	FROM meme AS s 
	WHERE s.share = m.idmeme
	OR s.share = m.share
) AS reposts,
1 AS repostable,
COUNT(reply.idreply) AS comments,
COUNT(star.iduser) AS stars
FROM meme AS m

-- user who did this post
LEFT JOIN user as p ON m.iduser = p.iduser

-- get the details of the original poster
LEFT JOIN user AS o ON o.iduser = (
    SELECT om.iduser
    FROM meme AS om
    WHERE om.idmeme = m.share
)

-- replies, stars & shares
LEFT JOIN reply ON reply.idmeme = m.idmeme
LEFT JOIN star ON star.idmeme = m.idmeme
-- LEFT JOIN meme AS s ON s.share = m.idmeme

-- future proof against scheduled posts
WHERE m.posted < CURRENT_TIMESTAMP
AND (
	-- show your own posts in the feed
	m.iduser = :id
	-- or ones you've followed
	OR m.iduser IN (
		SELECT followee
		FROM follow
		WHERE follower = :id
	)
)
GROUP BY m.idmeme
ORDER BY m.posted DESC
LIMIT 20 OFFSET :start";

			$sth = $dbh->prepare($sql);
			$sth->bindParam(':id',$user->iduser);
			$sth->bindParam(':start',$start, PDO::PARAM_INT);
			$sth->execute();

			$limitComments = true; // as this is a list we don't want to send hundreds of comments

			foreach ($sth->fetchAll() as $row) {
				$sizes = json_decode($row['sizes'],true);

				if($sizes == null) {
					$memes['error'] = "There was an error interpretting the meme size for {$row['idmeme']}";
					goto error; // just return the error and don't do any more
				}

				$pref = array( // what was asked for
					'thumb' => $thumb,
					'full' => $full
				);

				$chosen = array(); // what they'll get

				foreach ($sizes as $kind => $ksizes) {
					asort($ksizes); // ascending order
					foreach ($ksizes as $size) { // keep going till we find the first one bigger than what they asked for
						$chosen[$kind] = $size;
						if($size >= $pref[$kind]) break;
					}
				}

				$images = array();

				foreach ($chosen as $kind => $size) {
					$images[$kind] = $res . $kind . '/' . $size . '/' . (empty($row['share']) ? $row['idmeme'] : $row['share']) . '.' . $row['ext'];
				}

				$meme = array(
					'idmeme' => $row['idmeme'],
					'link' => $web.$row['pUsername'].'/'.$row['idmeme'],
					'images' => $images,
					'sizes' => $chosen,
					'ext' => $row['ext'],
					'poster' => array (
						'iduser' => $row['iduser'],
						'link' => $web.$row['pUsername'],
						'username' => $row['pUsername'],
						'name' => $row['pName'],
						'pic' => $res.$row['pPicUri']
					),
					'time' => array(
						'epoch' => strtotime($row['posted']),
						'str' => $row['posted'],
						'ago' => ago(strtotime($row['posted']))
					),
					'caption' => $row['caption'],
					'lat' => $row['latitude'],
					'long' => $row['longitude'],
				);

				if(empty($row['share'])) // an original post
					$meme['original'] = false;
				else
					$meme['original'] = array(
						'idmeme' => $row['share'],
						'link' => $web.$row['oUsername'].'/'.$row['share'],
						'poster' => array (
							'iduser' => $row['oIduser'],
							'link' => $web.$row['oUsername'],
							'username' => $row['oUsername'],
							'name' => $row['oName'],
							'pic' => $res.$row['oPicUri']
						)
					);

				$meme['reposts-num'] = $row['reposts'];
				$meme['repostable'] = $row['repostable'];
				$meme['stars-num'] = $row['stars'];
				$meme['comments-num'] = $row['comments'];
				if($row['comments']) {
					$meme['comments'] = array();
					
					$sql = "
					SELECT reply.*, user.username, CONCAT(user.firstName,' ',user.surname) as name, user.picUri
					FROM reply, user
					WHERE reply.idmeme = {$row['idmeme']}
					AND reply.iduser = user.iduser
					ORDER BY reply.replyed DESC";
					if($limitComments) $sql .= " LIMIT 5";

					$csth = $dbh->prepare($sql);
					$csth->execute();

					foreach ($csth->fetchAll() as $crow) {
						$comment = array(
							'idcomment' => $crow['idreply'],
							'comment' => $crow['reply'],
							'commenter' => array (
								'iduser' => $crow['iduser'],
								'username' => $crow['username'],
								'link' => $web.$crow['username'],
								'name' => $crow['name'],
								'pic' => $res.$crow['picUri']
							),
							'time' => array(
								'epoch' => strtotime($crow['replyed']),
								'str' => $crow['replyed'],
								'ago' => ago(strtotime($crow['replyed']))
							),
						);

						array_push($meme['comments'],$comment);
					}

					// so when it's limited to the last five they come up in the right order
					$meme['comments'] = array_reverse($meme['comments']); 
				}

				array_push($memes, $meme);
			}
		}
		catch (PDOException $e) {
			$memes['error'] = "There was an error retreiving memes from the database $e";
		}

		error:

		return $memes;
	}

	function ago($date) {
		// a function for simply putting how long ago something happened
		// $date should be an epoch integer
		// return a string, like "5d"

		$minute = 60;
		$hour = 60*$minute;
		$day = 24*$hour;
		$week = 7*$day;
		$year = 52*$week; // simplicities sake

		if(!($date = intval($date)))
			return false;
		if(($diff = time() - $date) <= $minute)
			return $diff.'s';
		if($diff < $hour)
			return floor($diff/$minute).'m';
		if($diff < $day)
			return floor($diff/$hour).'h';
		if($diff < $week)
			return floor($diff/$day).'d';
		if($diff < $year)
			return floor($diff/$week).'w';
		else
			return floor($diff/$year).'y';

		return false; // if it for some reason didn't get caught by the if statement
	}
?>