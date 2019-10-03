require "docker"
require "serverspec"

    describe "Dockerfile" do
      before(:all) do
        
        @image = Docker::Image.build_from_dir('.')

        set :os, family: :debian
        set :backend, :docker
        set :docker_image, @image.id
      end

        describe user('javauser') do
          it { should exist }
        end

        describe service('petclinic') do
         it { should be_running }
        end

        describe file('/home/javauser/') do
         it { should be_directory }
        end


end
