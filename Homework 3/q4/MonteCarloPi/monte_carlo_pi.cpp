#include <omp.h>

#include <iostream>
#include <cstdlib>
#include <cstring>
#include <stdlib.h>
#include <time.h>

int inline imax(int x, int y){
  return x > y ? x : y;
}

double MonteCarloPi(int s)
{
  #define THREAD_COUNT 8
  // #define MC_DEBUG true

  using namespace std;

  int valid = 0, samples = 0, i = 0;
  unsigned int seed;
  double x, y;

  omp_set_num_threads(THREAD_COUNT);

  #pragma omp parallel private(x, y,samples, seed, i) reduction(+:valid) 
  {
    samples = s;
    seed = (double) time(NULL) * (double) (omp_get_thread_num() + rand());
    
    #ifdef MC_DEBUG
    cout << omp_get_thread_num() << ": " << rand_r(&seed) << endl;
    int count = 0;
    #endif

    #pragma omp for private(i)
    for (i = 0; i < samples; i++)
    {
      x = ((double)rand_r(&seed)) / ((double)RAND_MAX);
      y = ((double)rand_r(&seed)) / ((double)RAND_MAX);

      #ifdef MC_DEBUG
      count++;
      #endif

      if ((x * x + y * y) <= 1.0){
        ++valid;
      }
    }

    #ifdef MC_DEBUG
    cout << count << endl;
    #endif

  }
  
  #ifdef MC_DEBUG
  cout << valid << " / " << s << endl;
  #endif

  return (((double)valid) / ((double)s)) * 4.0;
}

int main(int argc, char* argv[])
{
  using namespace std;

  if(argc < 2){
    cout << "Error: Arguments length < 1" << endl;
    return EXIT_FAILURE;
  }

  cout << MonteCarloPi(atoi(argv[1])) << endl;

  return EXIT_SUCCESS;
}