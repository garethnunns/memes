<?php
	require_once '../site/web.php';
	check();

	require_once dirname(__FILE__).'/../site/aws/aws-autoloader.php';
	use Aws\S3\S3Client;
	use Aws\CommandPool;
	
	$file = $_FILES['file'];

	header("refresh:5; url=home"); // fallback for none JS browsers

	echo json_encode(addMeme($_SESSION['key'],$file,$_POST['caption']));
?>