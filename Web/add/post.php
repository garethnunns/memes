<?php
	require_once '../site/functions.php';
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
		try {
			// store in db
			$sizes = array();

			foreach ($images as $type => $csizes)
				foreach ($csizes as $size => $image) {
					if(!isset($sizes[$type])) $sizes[$type] = array();
					array_push($sizes[$type], $size);
				}

			$sth = $dbh->prepare("INSERT INTO meme (iduser,sizes,caption) 
				VALUES (?, ?, ?)");

			$sth->execute(array(
				$_SESSION['user'],
				json_encode($sizes),
				$_POST['caption']
			));

			$id = $dbh->lastInsertId();
			//header("Location: /?created");

			$return['created'] = $images; // for now
			$return['success'] = true;
		}
		catch (PDOException $e) {
			$return['error'] = $e->getMessage();
		}

		if(empty($return['error'])) {
			// aws

			$s3 = S3Client::factory(array(
				'region'  => 'eu-west-2',
				'version' => '2006-03-01',
				'credentials' => array(
					'key'    => AWS_KEY,
					'secret' => AWS_SECRET,
				)
			));

			$bucket = 'memes-store';

			$commands = array();

			foreach ($images as $type => $csizes) {
				foreach ($csizes as $size => $image) {
					$commands[] = $s3->getCommand('PutObject', array(
						'Bucket'     => $bucket,
						'Key'        => $type.'/'.$size.'/'.$id.'.'.strtolower(pathinfo($image,PATHINFO_EXTENSION)),
						'SourceFile' => $image,
						'Metadata'   => array(
							'User' => $_SESSION['user']
						)
					));
				}
			}

			$pool = new CommandPool($s3, $commands);

			// Initiate the pool transfers
			$promise = $pool->promise();

			// Force the pool to complete synchronously
			try {
				$result = $promise->wait();
			} catch (AwsException $e) {
				// handle the error.
			}
		}
	}

	echo json_encode($return);
?>