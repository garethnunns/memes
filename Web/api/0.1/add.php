<?php
	require_once '../../site/functions.php';

	require_once '../../site/aws/aws-autoloader.php';
	use Aws\S3\S3Client;
	use Aws\CommandPool;
	
	$file = $_FILES['file'];

	echo json_encode(addMeme($_POST['key'],$file,$_POST['caption']));
?>