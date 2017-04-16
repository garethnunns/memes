<?php
	require_once '../site/functions.php';
	check();
	
	$file = $_FILES['file'];

	// what we'll convert into JSON to return to the user
	$return = array(
		'success' => false
	);

	if(($errors = resizeImage($file, $images)) !== true) // there were errors validating the image (see functions file)
		foreach ($errors as $key => $error)
			$return['error'] .= $error . ". ";
	else {
		$return['created'] = $images; // for now
		$return['success'] = true;
	}

	echo json_encode($return);
?>