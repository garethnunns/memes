<?php
	require_once '../../site/functions.php';

	echo json_encode(memeHotFeed($_POST['key'],$_POST['page'],$_POST['thumb'],$_POST['full']));
?>