package org.cloudbus.cloudsim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.TopologyParamsT;
import org.cloudbus.cloudsim.ext.XFD;
import org.cloudbus.cloudsim.ext.gga.GGA;
import org.cloudbus.cloudsim.ext.gga.GaParamsT;
import org.cloudbus.cloudsim.ext.gga.Genotype;
import org.cloudbus.cloudsim.ext.gga.Problem;
import org.cloudbus.cloudsim.ext.gga.PropertiesReader;
import org.cloudbus.cloudsim.ext.gga.enums.PackingT;
import org.cloudbus.cloudsim.ext.utils.IOUtil;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;

public class AdvanceDatacenter extends Datacenter {
	private List<? extends Vm> vmQueue;
	
	private int vmQueueCapacity;		//vmQueue容量，到了这个值启动一次vm部署
	private int ggaGenerations;
	private CloudSimEventListener progressListener;
	private GaParamsT gaparams;
	private TopologyParamsT topologyParams;
	
	private int strategy;

	public AdvanceDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval, int vmQueueCapacity, int totalGens, CloudSimEventListener l, GaParamsT gaparams, TopologyParamsT topologyParams, int strategy) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		
		this.vmQueueCapacity = vmQueueCapacity;
		this.ggaGenerations = totalGens;
		this.progressListener = l;
		this.gaparams = gaparams;
		this.topologyParams = topologyParams;
		this.strategy = strategy;
		setVmQueue(new ArrayList<Vm>());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmQueue() {
		return (List<T>) vmQueue;
	}

	protected <T extends Vm> void setVmQueue(List<T> vmQueue) {
		this.vmQueue = vmQueue;
	}

	/**
     * Processes events or services that are available for this PowerDatacenter.
     *
     * @param ev    a Sim_event object
     *
     * @pre ev != null
     * @post $none
     */
    @Override
	public void processEvent(SimEvent ev) {
    	super.processEvent(ev);
    }
    
    /**
     * Process the event for an User/Broker who wants to create a VM
     * in this PowerDatacenter. This PowerDatacenter will then send the status back to
     * the User/Broker.
     *
     * @param ev   a Sim_event object
     * @param ack the ack
     *
     * @pre ev != null
     * @post $none
     */
    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	getVmQueue().add(vm);
    	
    	if (getVmQueue().size() == vmQueueCapacity) {
    		PropertiesReader properties = PropertiesReader.loader();
    		String oldGeno = properties.getString(vmQueueCapacity+"old");
    		
    		//System.out.println(oldGeno);
    		//System.exit(0);
    		
    		switch(strategy) {
    		case 0:
    			allocateVmsWithGGA(oldGeno);
    			break;
    		case 1:
    			allocateVmsWithXFD(PackingT.FFD, oldGeno);
    			break;
    		case 2:
    			allocateVmsWithXFD(PackingT.BFD, oldGeno);
    			break;
    		}
    		//allocateVmsWithGGA();
    		//allocateVmsWithFFD();
    		//allocateVmsWithBFD();
    	}
    }
    
    private void allocateVmsWithGGA(String oldGeno) {
    	
    	Genotype last = new Genotype(oldGeno);
    	
    	Problem problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, last);
    	
    	GGA gga = new GGA(progressListener, gaparams, getVmQueue().size());
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, ggaGenerations, new Random().nextInt(9999999));
    	
    	Genotype bestGeno = null;
		gga.InitializePopulation ();

		if (gga.Run()) {
			//TODO: 成功得到结果
			bestGeno = gga.getBestGeno();
		} else {
			//TODO: 如果不成功怎么样
		}
    		
    	gga.Close();
    	
    	/*
    	
    	//TODO: 临时代码，这部分要改的，试试再来一次调度
    	problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, bestGeno);
    	//gaparams.PopulationSize = problem.getNrOfItems();
    	//gaparams.N_Crossover = gaparams.PopulationSize / 2;
    	//gaparams.N_Mutation = gaparams.PopulationSize / 2;
    	gga = new GGA(progressListener, gaparams);
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, ggaGenerations, new Random().nextInt(9999999));
    	
    	bestGeno = null;
		gga.InitializePopulation ();

		if (gga.Run()) {
			bestGeno = gga.getBestGeno();
		} else {
		}    		
    	gga.Close();
    	
    	// 临时代码结束
    	*/
    	
    	allcateByGenotype(bestGeno, problem);
    }
    
    private void allocateVmsWithXFD(PackingT s, String oldGeno) {
    	Problem problem = new Problem();
    	int size = getVmQueue().size();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, null);
    	
    	XFD xfd = new XFD();
    	xfd.Initialize(problem);
    	
    	Genotype best = xfd.getResult(s);
    	
    	Genotype old = new Genotype(oldGeno);
    	
    	allcateByGenotype(best, problem);
    	
    	String outStr = s + "\n";
    	
    	try {
    		outStr += best.getStatics() + "\n";
    		outStr += "Distance is: " + problem.getDistance(best, old);
			IOUtil.writeFile(outStr, s + "-" + size + "-statics");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /*
    private void allocateVmsWithBFD() {
    	Problem problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, null);
    	
    	XFD xfd = new XFD();
    	xfd.Initialize(problem);
    	
    	Genotype best = xfd.getResult(PackingT.BFD);
    	
    	Genotype old = new Genotype("156 582 582 582 581 76 8 578 215 4 162 6 156 581 25 3 400 1 581 580 580 577 4 4 10 580 1 579 488 256 10 561 7 579 7 10 10 25 25 577 161 26 362 67 577 362 24 26 27 9 41 576 578 21 54 27 105 105 6 105 6 9 12 577 576 11 11 65 11 24 28 27 49 49 545 18 18 89 89 389 49 575 12 89 24 12 46 66 13 576 514 575 107 52 573 16 372 575 19 19 16 16 21 61 205 21 8 8 19 572 29 13 31 13 35 51 574 22 409 285 22 22 52 46 60 14 51 51 574 573 54 28 54 28 77 158 14 60 53 60 14 61 61 75 61 562 124 573 283 572 63 63 572 65 20 546 53 113 65 53 193 48 399 77 30 34 65 69 73 69 39 69 39 38 73 92 32 32 572 73 38 20 571 570 73 570 98 571 223 566 32 263 570 569 326 30 31 64 569 31 15 568 569 76 37 35 568 76 56 50 557 34 79 85 568 2 2 85 2 187 85 77 15 534 45 565 563 567 15 564 567 77 2 79 29 29 164 35 35 79 79 567 82 554 81 566 220 566 42 81 81 45 45 115 62 115 82 559 43 478 17 17 37 82 17 67 359 565 17 560 48 565 564 564 48 83 563 563 562 37 23 23 64 55 48 55 83 55 562 562 124 561 83 560 59 84 124 560 84 84 560 59 59 0 72 559 421 40 559 33 558 62 23 33 91 20 345 67 68 36 36 95 91 88 104 36 66 71 62 40 40 42 80 139 138 66 548 41 41 43 41 80 138 559 44 92 44 64 67 68 33 146 80 68 47 139 47 558 524 555 42 264 71 88 44 214 71 47 92 553 72 139 30 74 43 78 47 46 99 92 50 558 90 88 102 46 50 50 557 555 74 557 556 72 164 75 75 164 57 132 164 78 102 185 90 74 86 556 90 555 429 90 102 94 535 555 94 54 537 554 99 549 99 297 78 554 553 553 97 151 548 213 477 56 185 551 56 133 552 185 141 106 552 57 551 551 108 225 70 57 106 546 106 186 108 185 3 114 58 550 186 108 543 58 549 97 100 215 5 110 5 538 305 550 550 121 110 110 549 548 198 548 96 85 198 70 114 367 100 101 97 487 547 113 70 70 181 547 198 546 103 198 141 128 546 125 113 87 540 545 121 101 41 545 101 121 194 125 197 536 103 103 107 183 98 98 114 482 98 114 245 545 128 179 125 111 44 128 128 177 118 122 544 118 544 118 132 539 244 134 163 132 544 133 133 134 147 141 543 525 120 530 141 147 145 406 312 230 107 281 543 542 112 100 112 112 120 123 120 145 542 534 123 107 394 542 342 541 165 541 540 153 123 248 540 98 39 144 147 533 119 539 539 86 86 119 87 538 140 130 130 537 180 93 538 169 196 419 230 87 127 140 127 127 93 140 129 129 147 28 93 131 155 51 95 159 470 131 537 168 536 119 95 532 536 127 153 146 265 153 166 535 155 104 243 131 534 155 159 159 492 165 165 535 168 151 534 157 146 95 169 168 169 143 311 151 533 184 143 533 182 104 532 146 507 104 143 532 520 531 531 152 150 530 448 529 150 528 158 152 158 530 529 150 515 144 473 405 157 177 157 116 136 136 163 529 528 182 163 189 467 527 109 177 160 160 161 161 136 189 182 144 109 109 275 111 184 184 193 528 209 3 526 166 144 111 111 117 117 149 527 116 193 420 526 526 193 166 179 171 149 171 176 189 525 173 173 190 521 262 199 116 179 202 254 129 522 502 171 2 190 194 249 190 117 250 179 149 180 173 180 192 21 377 149 117 525 463 110 160 523 194 182 216 233 122 464 213 167 508 199 524 524 162 126 122 9 522 194 199 126 126 202 523 7 137 495 250 522 521 520 234 202 521 213 213 188 143 501 519 135 520 519 188 214 506 214 250 214 162 162 517 197 216 518 517 215 519 518 148 200 200 516 216 264 247 200 150 135 223 517 514 135 216 188 516 170 170 29 515 209 514 207 207 515 137 329 196 207 209 227 137 142 142 513 203 69 221 142 210 210 209 264 221 212 514 124 512 154 212 513 210 226 223 223 148 226 148 154 154 227 246 418 226 167 512 511 512 218 218 218 196 196 458 212 220 220 167 174 204 264 224 220 197 512 511 227 511 301 231 239 227 172 348 293 307 231 231 233 233 510 510 227 509 229 229 172 510 509 172 174 509 238 292 232 232 508 174 210 292 371 503 292 232 183 505 239 321 228 236 236 237 253 244 237 508 239 101 507 229 131 175 178 267 293 181 175 293 507 244 242 242 324 243 242 78 237 308 236 243 217 244 175 244 259 506 176 491 246 261 258 224 305 255 305 494 305 232 309 309 258 324 224 246 259 187 417 176 252 178 334 497 258 259 224 506 309 505 68 256 259 256 218 324 178 181 324 261 240 222 222 275 284 392 261 181 26 270 52 500 228 505 310 498 238 276 228 504 332 504 504 187 183 183 499 234 503 238 503 288 191 243 502 240 238 314 241 396 306 240 269 251 269 496 269 278 249 187 502 191 275 269 275 489 329 295 502 501 108 274 274 276 476 501 192 376 500 285 276 384 500 274 191 241 329 161 241 192 499 285 192 201 201 282 337 282 249 285 203 201 312 249 426 203 251 290 204 263 234 499 234 299 290 251 375 290 252 252 206 252 253 295 186 251 253 241 291 295 291 256 301 253 299 493 498 498 294 294 205 203 289 309 294 254 195 301 299 105 297 297 204 297 497 495 379 291 366 157 497 23 300 300 490 301 300 397 302 302 204 307 310 307 205 225 496 312 315 310 211 271 206 370 494 255 302 496 312 205 206 339 254 266 254 492 255 270 495 298 494 53 447 313 313 294 313 485 211 282 288 211 483 494 266 380 316 493 317 493 492 404 385 217 491 265 315 217 270 219 327 265 492 300 321 217 270 17 491 321 219 219 491 489 487 488 99 490 490 315 489 485 401 262 486 304 263 488 316 328 208 268 328 319 316 317 328 330 330 242 330 262 488 317 483 487 487 484 331 482 335 486 486 335 485 331 485 484 326 484 333 225 40 308 268 225 263 248 308 483 308 260 314 263 330 268 266 323 271 477 314 343 483 272 314 343 322 333 326 322 322 255 338 323 323 482 268 482 245 343 331 337 382 416 350 350 335 352 333 327 328 352 352 479 313 397 338 337 327 445 287 481 338 350 340 339 481 31 327 245 247 342 352 357 248 357 340 480 334 321 408 334 271 271 278 397 397 479 468 359 480 443 479 478 478 478 365 365 357 342 339 342 393 155 133 477 278 477 476 339 200 55 471 476 476 475 345 345 278 353 230 50 359 363 365 247 459 359 353 345 353 248 356 474 475 5 356 260 74 461 356 39 363 288 475 408 288 289 363 38 114 287 408 287 112 272 170 427 289 288 289 260 474 360 137 473 315 236 267 360 474 473 360 472 272 473 471 472 472 380 388 383 380 383 267 383 371 272 367 469 470 388 322 470 401 388 392 367 471 296 469 466 367 371 464 341 470 468 318 401 392 318 467 392 402 453 130 273 318 469 319 59 371 0 465 468 318 467 467 319 436 319 375 404 402 395 346 13 381 375 463 273 273 406 406 406 273 404 402 375 404 376 466 377 466 378 303 120 336 462 377 410 276 377 379 465 277 382 385 226 346 346 279 351 382 390 464 384 351 281 273 387 277 372 378 372 376 398 277 351 464 459 461 463 24 304 403 364 378 403 463 462 298 126 384 279 460 279 462 455 430 409 396 409 396 409 379 382 396 391 280 135 362 384 390 431 280 385 387 399 399 280 432 281 292 394 385 139 399 394 325 364 393 460 284 461 364 410 391 1 281 410 286 460 410 37 394 390 457 395 284 459 391 286 395 387 387 459 60 458 458 457 286 393 12 381 457 381 381 296 366 393 456 452 366 456 456 442 296 454 455 398 325 344 455 398 451 348 18 366 405 405 266 400 454 370 400 454 453 370 400 453 403 5 407 417 298 433 407 417 428 298 453 452 417 451 355 355 354 354 452 354 325 451 451 450 373 373 374 94 433 418 149 430 418 386 430 426 347 347 448 347 433 432 432 439 429 431 431 447 303 450 11 413 336 336 38 304 84 449 336 449 424 306 320 303 429 320 320 304 361 320 311 421 311 332 361 386 171 429 369 449 222 361 306 422 215 411 428 427 306 448 446 428 446 448 427 426 283 283 425 426 447 386 444 425 445 425 447 440 257 257 423 389 332 389 424 257 389 424 283 423 445 423 422 421 422 332 446 445 443 354 344 178 235 419 57 93 341 341 58 444 235 444 358 420 443 368 311 421 420 208 208 443 49 442 368 208 344 442 368 441 441 441 174 14 96 420 419 358 229 358 374 374 348 11 235 19 419 440 348 172 440 439 416 415 439 369 349 369 438 369 438 416 349 195 437 116 195 437 349 438 30 110 52 195 416 115 437 412 63 415 437 435 436 436 435 414 18 415 435 414 414 434 434 413 413 412 9 412 411 411 0 34 411 3 1 156 0 434 27 156  : 2 17 23 33 36 41 44 47 98 112 119 127 129 131 143 150 160 161 171 173 200 210 212 218 220 229 232 236 237 242 243 256 269 274 282 291 294 297 300 302 313 321 328 330 335 343 350 352 357 365 406 403 409 399 394 400 355 354 347 336 320 311 283 257 235 208 195 156 105 89 39 38 32 369 358 325 287 234 170 162 149 144 136 107 103 101 100 96 74 71 66 62 59 29 28 22 21 16 12 6 3 374 368 410 396 376 372 401 408 397 329 324 309 305 293 292 264 250 230 198 186 185 164 139 138 124 115 85 51 24 362 389 386 373 370 364 351 346 319 318 289 288 278 271 268 263 262 251 249 222 197 196 188 180 179 166 163 157 152 151 99 94 90 88 80 55 48 45 15 8 266 5 349 348 344 341 361 366 381 387 395 404 402 392 388 383 380 363 359 339 334 327 323 322 314 308 270 265 255 254 253 252 241 240 238 228 224 209 207 193 184 182 158 146 140 130 123 120 118 114 97 78 75 72 68 67 64 53 19 18 11 332 306 304 303 298 296 286 284 281 280 279 277 273 272 267 260 248 247 245 225 219 217 211 206 205 204 203 201 192 191 187 183 181 178 176 175 174 172 167 154 148 142 137 135 126 122 117 116 111 109 104 95 93 87 86 70 58 57 56 50 46 43 42 40 37 35 34 31 30 20 14 13 9 1 0 411 412 413 414 415 416 419 420 421 422 423 424 425 426 427 428 429 431 432 433 430 418 417 407 405 398 393 391 390 385 384 382 379 378 377 375 371 367 360 356 353 345 342 340 338 337 333 331 326 317 316 315 312 310 307 301 299 295 290 285 276 275 261 259 258 246 244 239 233 231 227 226 223 221 216 214 213 202 199 194 190 189 177 169 168 165 159 155 153 147 145 141 134 133 132 128 125 121 113 110 108 106 102 92 91 84 83 82 81 79 77 76 73 69 65 63 61 60 54 52 49 27 26 25 10 7 4 215 434 435 436 437 438 439 440 441 442 443 444 445 446 447 448 449 450 451 452 453 454 455 456 457 458 459 460 461 462 463 464 465 466 467 468 469 470 471 472 473 474 475 476 477 478 479 480 481 482 483 484 485 486 487 488 489 490 491 492 493 494 495 496 497 498 499 500 501 502 503 504 505 506 507 508 509 510 511 512 513 514 515 516 517 518 519 520 521 522 523 524 525 526 527 528 529 530 531 532 533 534 535 536 537 538 539 540 541 542 543 544 545 546 547 548 549 550 551 552 553 554 555 556 557 558 559 560 561 562 563 564 565 566 567 568 569 570 571 572 573 574 575 576 577 578 579 580 581 582");
    	
    	allcateByGenotype(best, problem);
    	
    	System.out.println(best.getStatics());
    	
    	System.out.println("Distance is: " + problem.getDistance(best, old));
    }*/
    
    private void allcateByGenotype(Genotype geno, Problem problem) {
    	int size = getVmQueue().size();
    	String plan = "";
    	for (int i=0; i < size; i++) {
    		Vm vm = getVmQueue().get(i);
    		int host = problem.getHostAllocated(geno, i);//geno.getAllocatedHost(i);
    		plan += host + " ";
    		//System.out.println("Vm " + i + "size" +vm.getMips());
    		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, getHostList().get(host));
    		int[] data = new int[3];
            data[0] = getId();
  	       	data[1] = vm.getId();
    		if (result) {
         	   data[2] = CloudSimTags.TRUE;
            } else {
         	   data[2] = CloudSimTags.FALSE;
            }
 		   	sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
			if (result) {
				double amount = 0.0;
				if (getDebts().containsKey(vm.getUserId())) {
					amount = getDebts().get(vm.getUserId());
				}
				amount += getCharacteristics().getCostPerMem() * vm.getRam();
				amount += getCharacteristics().getCostPerStorage()
						* vm.getSize();

				getDebts().put(vm.getUserId(), amount);

				getVmList().add(vm);

				vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy()
						.getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
			} else {
				System.err.println("GGA Seems to be failed");
				System.out.println("Host: " + problem.GetBinSize(host));
				//assert(3==2);
			}
    		
    	}
    	
    	try {
			IOUtil.writeFile(plan,  strategy + "-stg-" + size + "-allocation");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	System.out.println("VM ALLOCATON: " + plan);
    	
    	getVmQueue().clear();
    }
}
