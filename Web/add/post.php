<?php
	require_once '../site/web.php';
	check();

	require_once dirname(__FILE__).'/../site/aws/aws-autoloader.php';
	use Aws\S3\S3Client;
	use Aws\CommandPool;
	
	$file = $_FILES['file'];

	// what we'll convert into JSON to return to the user
	$return = array(
		'success' => false
	);

	if(($error = valid('meme.caption',$_POST['caption'])) !== true)
		$return['error'] = $error;
	elseif(($errors = resizeImage($file, $images)) !== true) // there were errors validating the image (see functions file)
		foreach ($errors as $key => $error)
			$return['error'] .= $error . ". ";
	else {
		$link = '';
		if(($error = storeMeme($_SESSION['key'],$images,$_POST['caption'],null, null, $link)) !== true)
			$return['error'] = $error;
		else {
			$return['success'] = true;
			$return['link'] = $link;
		}
	}

	header("refresh:5; url=home"); // fallback for none JS browsers
	echo json_encode($return);
?>